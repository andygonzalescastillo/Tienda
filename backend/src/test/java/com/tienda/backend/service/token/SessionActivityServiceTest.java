package com.tienda.backend.service.token;

import com.tienda.backend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionActivityService")
class SessionActivityServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private SessionActivityService service;

    @Nested
    @DisplayName("registrarActividad")
    class RegistrarActividad {

        @Test
        @DisplayName("Debe actualizar último acceso en BD la primera vez")
        void debeActualizarUltimoAccesoPrimeraVez() {
            when(refreshTokenRepository.updateUltimoAcceso(eq("jti-1"), any(Instant.class))).thenReturn(1);

            service.registrarActividad("jti-1");

            verify(refreshTokenRepository).updateUltimoAcceso(eq("jti-1"), any(Instant.class));
        }

        @Test
        @DisplayName("No debe actualizar si se llama dentro del umbral de 5 minutos")
        void noDebeActualizarDentroDelUmbral() {
            when(refreshTokenRepository.updateUltimoAcceso(eq("jti-2"), any(Instant.class))).thenReturn(1);

            service.registrarActividad("jti-2");
            service.registrarActividad("jti-2");

            verify(refreshTokenRepository, times(1)).updateUltimoAcceso(eq("jti-2"), any(Instant.class));
        }

        @Test
        @DisplayName("No debe hacer nada si el JTI es null")
        void noDebeHacerNadaSiJtiEsNull() {
            service.registrarActividad(null);

            verifyNoInteractions(refreshTokenRepository);
        }

        @Test
        @DisplayName("No debe hacer nada si el JTI está en blanco")
        void noDebeHacerNadaSiJtiEstaEnBlanco() {
            service.registrarActividad("  ");

            verifyNoInteractions(refreshTokenRepository);
        }

        @Test
        @DisplayName("No debe actualizar cache si la BD no afectó filas")
        void noDebeActualizarCacheSiBdNoAfectoFilas() {
            when(refreshTokenRepository.updateUltimoAcceso(eq("jti-inexistente"), any(Instant.class))).thenReturn(0);

            service.registrarActividad("jti-inexistente");
            service.registrarActividad("jti-inexistente");

            verify(refreshTokenRepository, times(2)).updateUltimoAcceso(eq("jti-inexistente"), any(Instant.class));
        }

        @Test
        @DisplayName("Debe capturar excepción sin propagar")
        void debeCapturaExcepcionSinPropagar() {
            when(refreshTokenRepository.updateUltimoAcceso(anyString(), any(Instant.class)))
                    .thenThrow(new RuntimeException("DB error"));

            service.registrarActividad("jti-error");

            verify(refreshTokenRepository).updateUltimoAcceso(eq("jti-error"), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("removerDeCache")
    class RemoverDeCache {

        @Test
        @DisplayName("Debe permitir nueva actualización después de remover de cache")
        void debePermitirNuevaActualizacionDespuesDeRemover() {
            when(refreshTokenRepository.updateUltimoAcceso(eq("jti-3"), any(Instant.class))).thenReturn(1);

            service.registrarActividad("jti-3");
            service.removerDeCache("jti-3");
            service.registrarActividad("jti-3");

            verify(refreshTokenRepository, times(2)).updateUltimoAcceso(eq("jti-3"), any(Instant.class));
        }

        @Test
        @DisplayName("No debe fallar si el JTI es null")
        void noDebeFallarSiJtiEsNull() {
            service.removerDeCache(null);
        }
    }

    @Nested
    @DisplayName("limpiarCacheObsoleto")
    class LimpiarCacheObsoleto {

        @Test
        @DisplayName("Debe ejecutar limpieza sin errores")
        void debeEjecutarLimpiezaSinErrores() {
            when(refreshTokenRepository.updateUltimoAcceso(anyString(), any(Instant.class))).thenReturn(1);
            service.registrarActividad("jti-cache-1");
            service.registrarActividad("jti-cache-2");

            service.limpiarCacheObsoleto();

            service.registrarActividad("jti-cache-1");
            service.registrarActividad("jti-cache-2");

            verify(refreshTokenRepository, times(1)).updateUltimoAcceso(eq("jti-cache-1"), any(Instant.class));
            verify(refreshTokenRepository, times(1)).updateUltimoAcceso(eq("jti-cache-2"), any(Instant.class));
        }
    }
}
