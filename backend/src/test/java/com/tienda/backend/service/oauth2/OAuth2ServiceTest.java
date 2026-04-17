package com.tienda.backend.service.oauth2;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.entity.UserProvider;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service")
class OAuth2ServiceTest {

    @Mock private UserRepository usuarioRepository;

    @InjectMocks
    private OAuth2Service service;

    @Nested
    @DisplayName("procesarUsuarioOAuth2 - Usuario nuevo")
    class UsuarioNuevo {

        @Test
        @DisplayName("Debe crear usuario nuevo con proveedor Google")
        void debeCrearUsuarioNuevoConGoogle() {
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(usuarioRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.procesarUsuarioOAuth2("test@gmail.com", "Juan", "Pérez", "GOOGLE", "google-123");

            assertThat(result.getEmail()).isEqualTo("test@gmail.com");
            assertThat(result.getNombre()).isEqualTo("Juan");
            assertThat(result.getApellido()).isEqualTo("Pérez");
            assertThat(result.getEmailVerificado()).isTrue();
            assertThat(result.getEstado()).isTrue();
            assertThat(result.getRol()).isEqualTo(UserRole.USER);
            assertThat(result.hasProvider(AuthProvider.GOOGLE)).isTrue();
            verify(usuarioRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Debe crear usuario con nombre vacío si no se proporciona")
        void debeCrearUsuarioConNombreVacio() {
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(usuarioRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.procesarUsuarioOAuth2("test@gmail.com", null, null, "FACEBOOK", "fb-123");

            assertThat(result.getNombre()).isEmpty();
            assertThat(result.getApellido()).isEmpty();
            assertThat(result.hasProvider(AuthProvider.FACEBOOK)).isTrue();
        }
    }

    @Nested
    @DisplayName("procesarUsuarioOAuth2 - Usuario existente")
    class UsuarioExistente {

        @Test
        @DisplayName("Debe agregar nuevo proveedor a usuario existente")
        void debeAgregarNuevoProveedorAUsuarioExistente() {
            var user = User.builder()
                    .email("test@gmail.com")
                    .nombre("Juan")
                    .emailVerificado(false)
                    .estado(false)
                    .rol(UserRole.USER)
                    .build();
            user.addProvider(AuthProvider.LOCAL, null);

            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(usuarioRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.procesarUsuarioOAuth2("test@gmail.com", "Juan", "Pérez", "GOOGLE", "google-456");

            assertThat(result.hasProvider(AuthProvider.GOOGLE)).isTrue();
            assertThat(result.hasProvider(AuthProvider.LOCAL)).isTrue();
            assertThat(result.getEmailVerificado()).isTrue();
            assertThat(result.getEstado()).isTrue();
        }

        @Test
        @DisplayName("Debe actualizar providerId si el proveedor ya existe con ID diferente")
        void debeActualizarProviderIdSiYaExiste() {
            var user = User.builder()
                    .email("test@gmail.com")
                    .emailVerificado(true)
                    .estado(true)
                    .build();

            var googleProvider = UserProvider.builder()
                    .provider(AuthProvider.GOOGLE)
                    .providerId("old-google-id")
                    .user(user)
                    .build();
            user.setProviders(new java.util.HashSet<>(Set.of(googleProvider)));

            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(usuarioRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            service.procesarUsuarioOAuth2("test@gmail.com", "Juan", "Pérez", "GOOGLE", "new-google-id");

            assertThat(googleProvider.getProviderId()).isEqualTo("new-google-id");
            verify(usuarioRepository).save(user);
        }
    }

    @Nested
    @DisplayName("procesarUsuarioOAuth2 - Validaciones")
    class Validaciones {

        @Test
        @DisplayName("Debe lanzar excepción si providerId está vacío")
        void debeLanzarExcepcionSiProviderIdVacio() {
            assertThatThrownBy(() -> service.procesarUsuarioOAuth2("test@gmail.com", "Juan", "Pérez", "GOOGLE", ""))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("OAUTH2_PROVIDER_ID_MISSING");
        }

        @Test
        @DisplayName("Debe lanzar excepción si providerId es null")
        void debeLanzarExcepcionSiProviderIdNull() {
            assertThatThrownBy(() -> service.procesarUsuarioOAuth2("test@gmail.com", "Juan", "Pérez", "GOOGLE", null))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("OAUTH2_PROVIDER_ID_MISSING");
        }

        @Test
        @DisplayName("Debe lanzar excepción si el proveedor es inválido")
        void debeLanzarExcepcionSiProveedorInvalido() {
            assertThatThrownBy(() -> service.procesarUsuarioOAuth2("test@gmail.com", "Juan", "Pérez", "INVALID_PROVIDER", "id-123"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("OAUTH2_AUTHENTICATION_FAILED");
        }

        @Test
        @DisplayName("Debe aceptar proveedor en minúsculas")
        void debeAceptarProveedorEnMinusculas() {
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(usuarioRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.procesarUsuarioOAuth2("test@gmail.com", "Juan", "Pérez", "google", "google-123");

            assertThat(result.hasProvider(AuthProvider.GOOGLE)).isTrue();
        }
    }
}
