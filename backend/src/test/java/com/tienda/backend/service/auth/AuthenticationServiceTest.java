package com.tienda.backend.service.auth;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.auth.request.LoginRequest;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.SessionValidationResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.token.TokenValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService - Autenticación de usuarios")
class AuthenticationServiceTest {

    @Mock
    private UserRepository usuarioRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenValidationService tokenValidationService;

    @Mock
    private JwtUtils jwtUtilidad;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User usuarioActivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = User.builder()
                .id(1L)
                .email("test@mail.com")
                .nombre("Juan")
                .apellido("Pérez")
                .rol(UserRole.USER)
                .emailVerificado(true)
                .estado(true)
                .build();
    }

    @Nested
    @DisplayName("autenticarUsuario")
    class AutenticarUsuario {

        private final LoginRequest loginRequest = new LoginRequest("test@mail.com", "Password123");
        private final String ip = "192.168.1.1";
        private final String userAgent = "Mozilla/5.0";

        @Test
        @DisplayName("Debe autenticar exitosamente con credenciales válidas")
        void debeAutenticarConCredencialesValidas() {
            LoginResult resultEsperado = new LoginResult(
                    "access-token", "refresh-token", "LOGIN_SUCCESS",
                    "test@mail.com", "Juan Pérez", "USER"
            );

            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioActivo));
            when(tokenService.generarParDeTokens(usuarioActivo, ip, userAgent))
                    .thenReturn(resultEsperado);

            LoginResult resultado = authenticationService.autenticarUsuario(loginRequest, ip, userAgent);

            assertThat(resultado.email()).isEqualTo("test@mail.com");
            assertThat(resultado.accessToken()).isEqualTo("access-token");
            assertThat(resultado.refreshToken()).isEqualTo("refresh-token");
            assertThat(resultado.successCode()).isEqualTo("LOGIN_SUCCESS");
            assertThat(resultado.rol()).isEqualTo("USER");

            verify(authenticationManager).authenticate(
                    any(UsernamePasswordAuthenticationToken.class)
            );
            verify(eventPublisher).publishEvent(any(Object.class));
            verify(tokenService).generarParDeTokens(usuarioActivo, ip, userAgent);
        }

        @Test
        @DisplayName("Debe lanzar INVALID_CREDENTIALS cuando la contraseña es incorrecta")
        void debeLanzarErrorConCredencialesInvalidas() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authenticationService.autenticarUsuario(loginRequest, ip, userAgent))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
                        assertThat(appEx.getStatus().value()).isEqualTo(401);
                    });

            verify(usuarioRepository, never()).findByEmailAndEstadoTrue(any());
            verify(tokenService, never()).generarParDeTokens(any(), any(), any());
        }

        @Test
        @DisplayName("Debe lanzar INVALID_SESSION cuando el usuario no existe o está desactivado")
        void debeLanzarErrorConUsuarioInexistente() {
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.autenticarUsuario(loginRequest, ip, userAgent))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("INVALID_SESSION");
                        assertThat(appEx.getStatus().value()).isEqualTo(401);
                    });

            verify(tokenService, never()).generarParDeTokens(any(), any(), any());
        }

        @Test
        @DisplayName("Debe publicar evento UserAuthenticatedEvent tras login exitoso")
        void debePublicarEventoTrasLoginExitoso() {
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioActivo));
            when(tokenService.generarParDeTokens(any(), any(), any()))
                    .thenReturn(new LoginResult("at", "rt", "LOGIN_SUCCESS", "test@mail.com", "Juan", "USER"));

            authenticationService.autenticarUsuario(loginRequest, ip, userAgent);

            verify(eventPublisher, times(1)).publishEvent(any(Object.class));
        }
    }

    @Nested
    @DisplayName("validarSesionActual")
    class ValidarSesionActual {

        @Test
        @DisplayName("Debe retornar datos de sesión para usuario válido con sesión activa")
        void debeRetornarDatosDeSesionValida() {
            when(jwtUtilidad.obtenerRefreshTokenJti("access-token")).thenReturn("refresh-jti");
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti")).thenReturn(false);
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioActivo));

            SessionValidationResponse response = authenticationService.validarSesionActual("test@mail.com", "access-token");

            assertThat(response.email()).isEqualTo("test@mail.com");
            assertThat(response.nombre()).isEqualTo("Juan Pérez");
            assertThat(response.rol()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Debe lanzar SESSION_REVOKED cuando el refresh token fue revocado")
        void debeLanzarErrorConSesionRevocada() {
            when(jwtUtilidad.obtenerRefreshTokenJti("access-token")).thenReturn("refresh-jti");
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti")).thenReturn(true);

            assertThatThrownBy(() -> authenticationService.validarSesionActual("test@mail.com", "access-token"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("SESSION_REVOKED");
                    });

            verify(usuarioRepository, never()).findByEmailAndEstadoTrue(any());
        }

        @Test
        @DisplayName("Debe lanzar INVALID_SESSION cuando el usuario no existe")
        void debeLanzarErrorConUsuarioInexistente() {
            when(jwtUtilidad.obtenerRefreshTokenJti("access-token")).thenReturn("refresh-jti");
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti")).thenReturn(false);
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.validarSesionActual("test@mail.com", "access-token"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("INVALID_SESSION");
                    });
        }

        @Test
        @DisplayName("Debe lanzar INVALID_SESSION cuando el email no está verificado")
        void debeLanzarErrorConEmailNoVerificado() {
            usuarioActivo.setEmailVerificado(false);

            when(jwtUtilidad.obtenerRefreshTokenJti("access-token")).thenReturn("refresh-jti");
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti")).thenReturn(false);
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioActivo));

            assertThatThrownBy(() -> authenticationService.validarSesionActual("test@mail.com", "access-token"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("INVALID_SESSION");
                    });
        }

        @Test
        @DisplayName("Debe normalizar el email con espacios y mayúsculas")
        void debeNormalizarEmail() {
            when(jwtUtilidad.obtenerRefreshTokenJti("access-token")).thenReturn("refresh-jti");
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti")).thenReturn(false);
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioActivo));

            authenticationService.validarSesionActual("  TEST@MAIL.COM  ", "access-token");

            verify(usuarioRepository).findByEmailAndEstadoTrue("test@mail.com");
        }
    }
}
