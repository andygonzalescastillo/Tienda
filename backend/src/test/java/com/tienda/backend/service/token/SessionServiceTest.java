package com.tienda.backend.service.token;

import com.tienda.backend.config.properties.JwtProperties;
import com.tienda.backend.domain.entity.RefreshToken;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.auth.response.SessionResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.mapper.SessionMapper;
import com.tienda.backend.repository.RefreshTokenRepository;
import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.util.DeviceDetector;
import com.tienda.backend.service.util.DeviceDetector.DeviceInfo;
import com.tienda.backend.service.util.GeoLocationService;
import com.tienda.backend.service.websocket.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService")
class SessionServiceTest {

    @Mock private JwtProperties jwtProperties;
    @Mock private RefreshTokenRepository refreshTokenRepo;
    @Mock private JwtUtils jwtUtilidad;
    @Mock private DeviceDetector deviceDetector;
    @Mock private SessionMapper sessionMapper;
    @Mock private GeoLocationService geoLocationService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private SessionService service;

    @Nested
    @DisplayName("guardarRefreshToken")
    class GuardarRefreshToken {

        @Test
        @DisplayName("Debe guardar refresh token con ubicación y hash")
        void debeGuardarRefreshToken() {
            var user = User.builder().id(1L).email("test@mail.com").rol(UserRole.USER).build();

            when(refreshTokenRepo.contarSesionesActivas(eq(1L), any(Instant.class))).thenReturn(0L);
            when(jwtUtilidad.obtenerJti("jwt-token")).thenReturn("jti-123");
            when(jwtUtilidad.obtenerFechaExpiracion("jwt-token")).thenReturn(new Date(System.currentTimeMillis() + 86400000));
            when(geoLocationService.obtenerUbicacion("192.168.1.1")).thenReturn("Lima, Perú");
            when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            service.guardarRefreshToken("jwt-token", user, "192.168.1.1", "Chrome/120");

            verify(refreshTokenRepo).save(argThat(token ->
                    token.getTokenId().equals("jti-123") &&
                    token.getIpAddress().equals("192.168.1.1") &&
                    token.getUserAgent().equals("Chrome/120") &&
                    token.getUbicacion().equals("Lima, Perú") &&
                    token.getTokenHash() != null && !token.getTokenHash().isEmpty()
            ));
        }

        @Test
        @DisplayName("Debe revocar sesión más antigua si se alcanza el límite")
        void debeRevocarSesionAntiguaSiLimiteAlcanzado() {
            var user = User.builder().id(1L).email("test@mail.com").rol(UserRole.USER).build();
            var sesionAntigua = RefreshToken.builder()
                    .tokenId("old-jti")
                    .user(user)
                    .revocado(false)
                    .build();

            when(jwtProperties.maxSessionsPerUser()).thenReturn(3);
            when(refreshTokenRepo.contarSesionesActivas(eq(1L), any(Instant.class))).thenReturn(3L);
            when(refreshTokenRepo.obtenerSesionesActivasOrdenadasPorAntiguedad(eq(1L), any(Instant.class)))
                    .thenReturn(List.of(sesionAntigua));
            when(jwtUtilidad.obtenerJti(anyString())).thenReturn("new-jti");
            when(jwtUtilidad.obtenerFechaExpiracion(anyString())).thenReturn(new Date(System.currentTimeMillis() + 86400000));
            when(geoLocationService.obtenerUbicacion(anyString())).thenReturn("Lima");
            when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            service.guardarRefreshToken("new-token", user, "10.0.0.1", "Firefox");

            verify(refreshTokenRepo, atLeast(2)).save(any(RefreshToken.class));
            assertThat(sesionAntigua.getRevocado()).isTrue();
            assertThat(sesionAntigua.getRazonRevocacion()).isEqualTo("LIMITE_SESIONES");
        }
    }

    @Nested
    @DisplayName("obtenerSesionesActivas")
    class ObtenerSesionesActivas {

        @Test
        @DisplayName("Debe retornar sesiones activas no expiradas")
        void debeRetornarSesionesActivas() {
            var token = RefreshToken.builder()
                    .tokenId("jti-1")
                    .userAgent("Chrome/120")
                    .fechaExpiracion(Instant.now().plus(12, ChronoUnit.HOURS))
                    .build();

            var deviceInfo = new DeviceInfo("Chrome en Windows", "DESKTOP");
            var sessionResponse = new SessionResponse(1L, "jti-1", 1L, Instant.now(), Instant.now().plus(12, ChronoUnit.HOURS),
                    "192.168.1.1", "Chrome/120", "Lima", false, true, "Chrome en Windows", "DESKTOP", Instant.now());

            when(refreshTokenRepo.findByUserIdAndRevocadoFalseOrderByFechaCreacionDesc(1L)).thenReturn(List.of(token));
            when(deviceDetector.detectar("Chrome/120")).thenReturn(deviceInfo);
            when(sessionMapper.toResponse(token, deviceInfo, true)).thenReturn(sessionResponse);

            var result = service.obtenerSesionesActivas(1L, "jti-1");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().tokenId()).isEqualTo("jti-1");
            assertThat(result.getFirst().esActual()).isTrue();
        }

        @Test
        @DisplayName("Debe filtrar sesiones expiradas")
        void debeFiltrarSesionesExpiradas() {
            var expired = RefreshToken.builder()
                    .tokenId("expired-jti")
                    .userAgent("Firefox")
                    .fechaExpiracion(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build();

            when(refreshTokenRepo.findByUserIdAndRevocadoFalseOrderByFechaCreacionDesc(1L)).thenReturn(List.of(expired));

            var result = service.obtenerSesionesActivas(1L, "current-jti");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarOpcionalPorTokenId")
    class BuscarOpcionalPorTokenId {

        @Test
        @DisplayName("Debe retornar Optional con token si existe")
        void debeRetornarTokenSiExiste() {
            var token = RefreshToken.builder().tokenId("jti-1").build();
            when(refreshTokenRepo.findByTokenIdAndRevocadoFalse("jti-1")).thenReturn(Optional.of(token));

            var result = service.buscarOpcionalPorTokenId("jti-1");

            assertThat(result).isPresent();
            assertThat(result.get().getTokenId()).isEqualTo("jti-1");
        }

        @Test
        @DisplayName("Debe retornar Optional vacío si no existe")
        void debeRetornarVacioSiNoExiste() {
            when(refreshTokenRepo.findByTokenIdAndRevocadoFalse("unknown")).thenReturn(Optional.empty());

            assertThat(service.buscarOpcionalPorTokenId("unknown")).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorTokenId")
    class BuscarPorTokenId {

        @Test
        @DisplayName("Debe retornar token y actualizar último acceso")
        void debeRetornarTokenYActualizarAcceso() {
            var token = RefreshToken.builder().tokenId("jti-1").build();
            when(refreshTokenRepo.findByTokenIdAndRevocadoFalse("jti-1")).thenReturn(Optional.of(token));
            when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.buscarPorTokenId("jti-1");

            assertThat(result.getTokenId()).isEqualTo("jti-1");
            assertThat(result.getUltimoAcceso()).isNotNull();
            verify(refreshTokenRepo).save(token);
        }

        @Test
        @DisplayName("Debe lanzar excepción si token no existe")
        void debeLanzarExcepcionSiNoExiste() {
            when(refreshTokenRepo.findByTokenIdAndRevocadoFalse("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorTokenId("unknown"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("TOKEN_REVOKED");
        }
    }

    @Nested
    @DisplayName("revocarRefreshTokenEntity")
    class RevocarRefreshTokenEntity {

        @Test
        @DisplayName("Debe revocar token con razón y fecha")
        void debeRevocarTokenConRazon() {
            var token = RefreshToken.builder().tokenId("jti-1").revocado(false).build();
            when(refreshTokenRepo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            service.revocarRefreshTokenEntity(token, "LOGOUT");

            assertThat(token.getRevocado()).isTrue();
            assertThat(token.getRazonRevocacion()).isEqualTo("LOGOUT");
            assertThat(token.getFechaRevocacion()).isNotNull();
            verify(refreshTokenRepo).save(token);
        }
    }

    @Nested
    @DisplayName("revocarTodosPorUsuario")
    class RevocarTodosPorUsuario {

        @Test
        @DisplayName("Debe revocar todos los tokens del usuario")
        void debeRevocarTodosLosTokens() {
            service.revocarTodosPorUsuario(1L, "LOGOUT_ALL");

            verify(refreshTokenRepo).revocarTodosPorUsuario(eq(1L), any(Instant.class), eq("LOGOUT_ALL"));
        }
    }
}
