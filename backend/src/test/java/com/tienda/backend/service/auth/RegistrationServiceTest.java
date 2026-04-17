package com.tienda.backend.service.auth;

import com.tienda.backend.config.properties.SecurityProperties;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.domain.enums.VerificationCodeType;
import com.tienda.backend.dto.auth.request.RegisterRequest;
import com.tienda.backend.dto.auth.request.VerifyCodeRequest;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.RegisterResponse;
import com.tienda.backend.dto.auth.response.VerifyEmailExistenceResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.service.redis.RedisOtpService;
import com.tienda.backend.service.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationService - Registro de usuarios")
class RegistrationServiceTest {

    @Mock
    private UserRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RedisOtpService redisOtpService;

    @Mock
    private SecurityProperties securityProperties;

    @InjectMocks
    private RegistrationService registrationService;

    private User usuarioVerificado;
    private User usuarioSinVerificar;
    private SecurityProperties.Otp otpConfig;

    @BeforeEach
    void setUp() {
        otpConfig = new SecurityProperties.Otp(5, 3);

        usuarioVerificado = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("hashed-password")
                .nombre("Juan")
                .apellido("Pérez")
                .rol(UserRole.USER)
                .emailVerificado(true)
                .estado(true)
                .build();
        usuarioVerificado.addProvider(AuthProvider.LOCAL, "LOCAL");

        usuarioSinVerificar = User.builder()
                .id(2L)
                .email("nuevo@mail.com")
                .password("hashed")
                .nombre("María")
                .apellido("García")
                .rol(UserRole.USER)
                .emailVerificado(false)
                .estado(true)
                .build();
    }

    @Nested
    @DisplayName("verificarEmailExistente")
    class VerificarEmailExistente {

        @Test
        @DisplayName("Debe retornar existe=true para usuario verificado con LOCAL")
        void debeRetornarExisteTrueConLocal() {
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioVerificado));

            VerifyEmailExistenceResponse response = registrationService.verificarEmailExistente("test@mail.com");

            assertThat(response.existe()).isTrue();
            assertThat(response.tienePassword()).isTrue();
            assertThat(response.proveedor()).isEqualTo("LOCAL");
        }

        @Test
        @DisplayName("Debe retornar existe=false para email inexistente")
        void debeRetornarExisteFalseParaInexistente() {
            when(usuarioRepository.findByEmailAndEstadoTrue("noexiste@mail.com"))
                    .thenReturn(Optional.empty());

            VerifyEmailExistenceResponse response = registrationService.verificarEmailExistente("noexiste@mail.com");

            assertThat(response.existe()).isFalse();
            assertThat(response.tienePassword()).isFalse();
            assertThat(response.proveedor()).isNull();
        }

        @Test
        @DisplayName("Debe retornar existe=false para usuario no verificado")
        void debeRetornarExisteFalseParaNoVerificado() {
            when(usuarioRepository.findByEmailAndEstadoTrue("nuevo@mail.com"))
                    .thenReturn(Optional.of(usuarioSinVerificar));

            VerifyEmailExistenceResponse response = registrationService.verificarEmailExistente("nuevo@mail.com");

            assertThat(response.existe()).isFalse();
        }
    }

    @Nested
    @DisplayName("registrarUsuarioSinVerificar")
    class RegistrarUsuarioSinVerificar {

        private final RegisterRequest request = new RegisterRequest(
                "nuevo@mail.com", "Password123", "María", "García"
        );

        @Test
        @DisplayName("Debe registrar usuario nuevo exitosamente")
        void debeRegistrarUsuarioNuevo() {
            when(usuarioRepository.findByEmail("nuevo@mail.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
            when(usuarioRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });
            when(securityProperties.otp()).thenReturn(otpConfig);

            RegisterResponse response = registrationService.registrarUsuarioSinVerificar(request);

            assertThat(response.email()).isEqualTo("nuevo@mail.com");
            assertThat(response.nombre()).isEqualTo("María García");
            assertThat(response.metadata()).containsEntry("expirationMinutes", 5L);

            verify(usuarioRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe lanzar EMAIL_ALREADY_VERIFIED si email ya verificado")
        void debeLanzarErrorSiEmailYaVerificado() {
            when(usuarioRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(usuarioVerificado));

            RegisterRequest reqExistente = new RegisterRequest(
                    "test@mail.com", "Password123", "Juan", "Pérez"
            );

            assertThatThrownBy(() -> registrationService.registrarUsuarioSinVerificar(reqExistente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("EMAIL_ALREADY_VERIFIED");
                        assertThat(appEx.getStatus().value()).isEqualTo(409);
                    });

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe permitir re-registrar usuario no verificado")
        void debePermitirReRegistrarNoVerificado() {
            when(usuarioRepository.findByEmail("nuevo@mail.com")).thenReturn(Optional.of(usuarioSinVerificar));
            when(passwordEncoder.encode("Password123")).thenReturn("new-encoded");
            when(usuarioRepository.save(any(User.class))).thenReturn(usuarioSinVerificar);
            when(securityProperties.otp()).thenReturn(otpConfig);

            RegisterResponse response = registrationService.registrarUsuarioSinVerificar(request);

            assertThat(response.email()).isEqualTo("nuevo@mail.com");
            verify(usuarioRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("verificarEmailYGenerarTokens")
    class VerificarEmailYGenerarTokens {

        private final VerifyCodeRequest verifyRequest = new VerifyCodeRequest("test@mail.com", "123456");
        private final String ip = "192.168.1.1";
        private final String ua = "Mozilla/5.0";

        @Test
        @DisplayName("Debe verificar código y generar tokens exitosamente")
        void debeVerificarYGenerarTokens() {
            LoginResult expectedResult = new LoginResult(
                    "access", "refresh", "LOGIN_SUCCESS", "test@mail.com", "Juan Pérez", "USER"
            );

            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioVerificado));
            when(tokenService.generarParDeTokens(any(User.class), eq(ip), eq(ua)))
                    .thenReturn(expectedResult);

            LoginResult result = registrationService.verificarEmailYGenerarTokens(verifyRequest, ip, ua);

            assertThat(result.email()).isEqualTo("test@mail.com");
            assertThat(result.accessToken()).isEqualTo("access");

            verify(redisOtpService).validarCodigo("test@mail.com", "123456", VerificationCodeType.REGISTRO);
            verify(redisOtpService).eliminarCodigo("test@mail.com", VerificationCodeType.REGISTRO);
            verify(usuarioRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Debe lanzar USER_NOT_FOUND si usuario no existe")
        void debeLanzarErrorSiUsuarioNoExiste() {
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> registrationService.verificarEmailYGenerarTokens(verifyRequest, ip, ua))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("USER_NOT_FOUND");
                        assertThat(appEx.getStatus().value()).isEqualTo(404);
                    });
        }
    }

    @Nested
    @DisplayName("reenviarCodigo")
    class ReenviarCodigo {

        @Test
        @DisplayName("Debe reenviar código a usuario no verificado")
        void debeReenviarCodigo() {
            when(usuarioRepository.findByEmail("nuevo@mail.com"))
                    .thenReturn(Optional.of(usuarioSinVerificar));
            when(securityProperties.otp()).thenReturn(otpConfig);

            MessageResponse response = registrationService.reenviarCodigo("nuevo@mail.com");

            assertThat(response.successCode()).isEqualTo("CODE_RESENT");
            assertThat(response.metadata()).containsEntry("expirationMinutes", 5L);
            verify(eventPublisher).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe lanzar EMAIL_ERROR si email ya verificado")
        void debeLanzarErrorSiYaVerificado() {
            when(usuarioRepository.findByEmail("test@mail.com"))
                    .thenReturn(Optional.of(usuarioVerificado));

            assertThatThrownBy(() -> registrationService.reenviarCodigo("test@mail.com"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("EMAIL_ERROR");
                    });
        }

        @Test
        @DisplayName("Debe lanzar EMAIL_ERROR si email no existe")
        void debeLanzarErrorSiNoExiste() {
            when(usuarioRepository.findByEmail("noexiste@mail.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> registrationService.reenviarCodigo("noexiste@mail.com"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("EMAIL_ERROR");
                    });
        }
    }
}
