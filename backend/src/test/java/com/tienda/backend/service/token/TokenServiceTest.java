package com.tienda.backend.service.token;

import com.tienda.backend.domain.entity.RefreshToken;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.websocket.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService - Gestión de tokens y sesiones")
class TokenServiceTest {

    @Mock
    private JwtUtils jwtUtilidad;
    @Mock
    private UserRepository usuarioRepo;
    @Mock
    private TokenBlacklistService blacklistService;
    @Mock
    private SessionService sessionService;
    @Mock
    private SessionActivityService sessionActivityService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TokenService tokenService;

    private User usuarioBase;
    private final String ip = "127.0.0.1";
    private final String ua = "Mozilla";

    @BeforeEach
    void setUp() {
        usuarioBase = User.builder()
                .id(1L)
                .email("test@mail.com")
                .nombre("Juan")
                .apellido("Pérez")
                .rol(UserRole.USER)
                .estado(true)
                .build();
    }

    @Nested
    @DisplayName("generarParDeTokens")
    class GenerarParDeTokens {

        @Test
        @DisplayName("Debe generar par de tokens e invocar servicios dependientes")
        void debeGenerarTokensExitosamente() {
            when(jwtUtilidad.generarRefreshToken(anyString(), anyLong(), anyString())).thenReturn("new-refresh");
            when(jwtUtilidad.obtenerJti("new-refresh")).thenReturn("refresh-jti");
            when(jwtUtilidad.generarAccessToken(anyString(), anyLong(), anyString(), eq("refresh-jti")))
                    .thenReturn("new-access");

            LoginResult result = tokenService.generarParDeTokens(usuarioBase, ip, ua);

            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");
            assertThat(result.email()).isEqualTo("test@mail.com");

            verify(sessionService).guardarRefreshToken("new-refresh", usuarioBase, ip, ua);
            verify(eventPublisher).publishEvent(any(Object.class));
        }
    }

    @Nested
    @DisplayName("refrescarToken")
    class RefrescarToken {

        @Test
        @DisplayName("Debe refrescar token válido rotándolo por uno nuevo")
        void debeRefrescarTokenValido() {
            String oldRefresh = "old-refresh";
            RefreshToken tokenEntity = new RefreshToken();
            tokenEntity.setUser(usuarioBase);

            when(jwtUtilidad.esTokenValido(oldRefresh)).thenReturn(true);
            when(jwtUtilidad.esRefreshToken(oldRefresh)).thenReturn(true);
            when(jwtUtilidad.obtenerJti(oldRefresh)).thenReturn("old-jti");
            when(sessionService.buscarOpcionalPorTokenId("old-jti")).thenReturn(Optional.of(tokenEntity));

            when(jwtUtilidad.generarRefreshToken(anyString(), anyLong(), anyString())).thenReturn("new-refresh");
            when(jwtUtilidad.obtenerJti("new-refresh")).thenReturn("new-jti");
            when(jwtUtilidad.generarAccessToken(anyString(), anyLong(), anyString(), eq("new-jti")))
                    .thenReturn("new-access");

            LoginResult result = tokenService.refrescarToken(oldRefresh, ip, ua);

            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");

            verify(sessionActivityService).removerDeCache("old-jti");
            verify(sessionService).actualizarRefreshToken(tokenEntity, "new-refresh");
        }

        @Test
        @DisplayName("Debe lanzar INVALID_REFRESH_TOKEN si el token tiene firma mala o expiró")
        void debeLanzarErrorSiFirmaMala() {
            when(jwtUtilidad.esTokenValido("malo")).thenReturn(false);

            assertThatThrownBy(() -> tokenService.refrescarToken("malo", ip, ua))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("INVALID_REFRESH_TOKEN");
        }

        @Test
        @DisplayName("Debe lanzar INVALID_TOKEN_TYPE si se usa un access token")
        void debeLanzarErrorSiTipoIncorrecto() {
            when(jwtUtilidad.esTokenValido("access-token-valido")).thenReturn(true);
            when(jwtUtilidad.esRefreshToken("access-token-valido")).thenReturn(false);

            assertThatThrownBy(() -> tokenService.refrescarToken("access-token-valido", ip, ua))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("INVALID_TOKEN_TYPE");
        }

        @Test
        @DisplayName("Debe lanzar REFRESH_TOKEN_NOT_FOUND si el token no está en BD (ya revocado/eliminado)")
        void debeLanzarErrorSiNoEstaEnBD() {
            when(jwtUtilidad.esTokenValido("token")).thenReturn(true);
            when(jwtUtilidad.esRefreshToken("token")).thenReturn(true);
            when(jwtUtilidad.obtenerJti("token")).thenReturn("jti");
            when(sessionService.buscarOpcionalPorTokenId("jti")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tokenService.refrescarToken("token", ip, ua))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("REFRESH_TOKEN_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("cerrarSesion y Revocaciones")
    class CerrarSesion {

        @Test
        @DisplayName("cerrarSesion debe revocar ambos tokens si son provistos")
        void debeCerrarAmbosTokens() {
            when(jwtUtilidad.esTokenValido("refresh")).thenReturn(true);
            when(jwtUtilidad.esRefreshToken("refresh")).thenReturn(true);
            when(jwtUtilidad.obtenerJti("refresh")).thenReturn("r-jti");
            RefreshToken mockEntity = new RefreshToken();
            mockEntity.setUser(usuarioBase);
            when(sessionService.buscarOpcionalPorTokenId("r-jti")).thenReturn(Optional.of(mockEntity));

            when(jwtUtilidad.esTokenValido("access")).thenReturn(true);
            when(jwtUtilidad.esAccessToken("access")).thenReturn(true);
            when(jwtUtilidad.obtenerFechaExpiracion("access")).thenReturn(new Date(System.currentTimeMillis() + 60000));
            when(jwtUtilidad.obtenerJti("access")).thenReturn("a-jti");

            tokenService.cerrarSesion("access", "refresh");

            verify(sessionActivityService).removerDeCache("r-jti");
            verify(sessionService).revocarRefreshTokenEntity(mockEntity, "LOGOUT");
            verify(blacklistService).agregarAListaNegra(eq("a-jti"), anyLong());
        }

        @Test
        @DisplayName("cerrarTodasLasSesionesDelUsuario debe invocar sessionService y WebSocket")
        void debeCerrarTodasLasSesiones() {
            when(usuarioRepo.findByEmailAndEstadoTrue("test@mail.com")).thenReturn(Optional.of(usuarioBase));

            tokenService.cerrarTodasLasSesionesDelUsuario("test@mail.com");

            verify(sessionService).revocarTodosPorUsuario(eq(usuarioBase.getId()), eq("LOGOUT_ALL"));
            verify(notificationService).enviarAUsuario(eq("test@mail.com"), anyString(), any());
        }

        @Test
        @DisplayName("cerrarSesionEspecifica debe revocar si pertenece al usuario")
        void debeCerrarSesionEspecifica() {
            String myEmail = "test@mail.com";
            when(usuarioRepo.findByEmailAndEstadoTrue(myEmail)).thenReturn(Optional.of(usuarioBase));

            RefreshToken targetToken = new RefreshToken();
            targetToken.setUser(usuarioBase);
            when(sessionService.buscarPorTokenId("target-jti")).thenReturn(targetToken);

            MessageResponse res = tokenService.cerrarSesionEspecifica("target-jti", myEmail, "current-jti");

            assertThat(res.successCode()).isEqualTo("SESSION_CLOSED");
            verify(sessionService).revocarRefreshTokenEntity(targetToken, "REVOCACION_MANUAL_USUARIO");
            verify(notificationService).enviarAUsuario(eq(myEmail), anyString(), any());
        }

        @Test
        @DisplayName("cerrarSesionEspecifica debe fallar al intentar cerrar la sesión actual (suicidio)")
        void debeFallarAlCerrarSesionActual() {
            assertThatThrownBy(() -> tokenService.cerrarSesionEspecifica("current-jti", "e@mail.com", "current-jti"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("CANNOT_CLOSE_CURRENT_SESSION");
        }

        @Test
        @DisplayName("cerrarSesionEspecifica debe fallar si el token pertenece a OTRO usuario")
        void debeFallarSiTargetDeOtroUsuario() {
            String myEmail = "test@mail.com";
            when(usuarioRepo.findByEmailAndEstadoTrue(myEmail)).thenReturn(Optional.of(usuarioBase));

            User otroUser = User.builder().id(2L).build();
            RefreshToken targetToken = new RefreshToken();
            targetToken.setUser(otroUser);
            when(sessionService.buscarPorTokenId("target-jti")).thenReturn(targetToken);

            assertThatThrownBy(() -> tokenService.cerrarSesionEspecifica("target-jti", myEmail, "current-jti"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("SESSION_ACCESS_DENIED");
        }
    }
}
