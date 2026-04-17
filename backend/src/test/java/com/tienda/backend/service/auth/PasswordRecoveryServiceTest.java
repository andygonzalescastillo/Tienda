package com.tienda.backend.service.auth;

import com.tienda.backend.config.properties.SecurityProperties;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.domain.enums.VerificationCodeType;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.service.redis.RedisOtpService;
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
@DisplayName("PasswordRecoveryService - Recuperación de contraseña")
class PasswordRecoveryServiceTest {

    @Mock
    private UserRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisOtpService redisOtpService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SecurityProperties securityProperties;

    @InjectMocks
    private PasswordRecoveryService passwordRecoveryService;

    private User usuarioConPassword;
    private SecurityProperties.Otp otpConfig;

    @BeforeEach
    void setUp() {
        otpConfig = new SecurityProperties.Otp(5, 3);

        usuarioConPassword = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("hashed-password")
                .nombre("Juan")
                .apellido("Pérez")
                .rol(UserRole.USER)
                .emailVerificado(true)
                .estado(true)
                .build();
    }

    @Nested
    @DisplayName("solicitarRecuperacionPassword")
    class SolicitarRecuperacion {

        @Test
        @DisplayName("Debe enviar OTP cuando el usuario existe con password y email verificado")
        void debeEnviarOtpConUsuarioValido() {
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioConPassword));
            when(securityProperties.otp()).thenReturn(otpConfig);

            MessageResponse response = passwordRecoveryService.solicitarRecuperacionPassword("test@mail.com");

            assertThat(response.successCode()).isEqualTo("OTP_SENT");
            assertThat(response.metadata()).containsEntry("expirationMinutes", 5L);
            verify(eventPublisher).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe retornar OTP_SENT sin enviar evento si email no existe (seguridad)")
        void debeRetornarOtpSentSinEventoSiNoExiste() {
            when(usuarioRepository.findByEmailAndEstadoTrue("noexiste@mail.com"))
                    .thenReturn(Optional.empty());
            when(securityProperties.otp()).thenReturn(otpConfig);

            MessageResponse response = passwordRecoveryService.solicitarRecuperacionPassword("noexiste@mail.com");

            assertThat(response.successCode()).isEqualTo("OTP_SENT");
            verify(eventPublisher, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe retornar OTP_SENT sin evento si usuario no tiene password (OAuth)")
        void debeRetornarOtpSentSinEventoSiSinPassword() {
            usuarioConPassword.setPassword(null);
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioConPassword));
            when(securityProperties.otp()).thenReturn(otpConfig);

            MessageResponse response = passwordRecoveryService.solicitarRecuperacionPassword("test@mail.com");

            assertThat(response.successCode()).isEqualTo("OTP_SENT");
            verify(eventPublisher, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe retornar OTP_SENT sin evento si email no verificado")
        void debeRetornarOtpSentSinEventoSiNoVerificado() {
            usuarioConPassword.setEmailVerificado(false);
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioConPassword));
            when(securityProperties.otp()).thenReturn(otpConfig);

            MessageResponse response = passwordRecoveryService.solicitarRecuperacionPassword("test@mail.com");

            assertThat(response.successCode()).isEqualTo("OTP_SENT");
            verify(eventPublisher, never()).publishEvent(any(Object.class));
        }
    }

    @Nested
    @DisplayName("verificarCodigoRecuperacion")
    class VerificarCodigo {

        @Test
        @DisplayName("Debe verificar código exitosamente")
        void debeVerificarCodigoExitoso() {
            MessageResponse response = passwordRecoveryService.verificarCodigoRecuperacion("test@mail.com", "123456");

            assertThat(response.successCode()).isEqualTo("CODE_VERIFIED");
            verify(redisOtpService).validarCodigo("test@mail.com", "123456", VerificationCodeType.RECUPERACION, false);
        }
    }

    @Nested
    @DisplayName("restablecerPassword")
    class RestablecerPassword {

        @Test
        @DisplayName("Debe restablecer password exitosamente")
        void debeRestablecerPasswordExitoso() {
            when(usuarioRepository.findByEmailAndEstadoTrue("test@mail.com"))
                    .thenReturn(Optional.of(usuarioConPassword));
            when(passwordEncoder.encode("NuevaPass123")).thenReturn("new-hash");

            MessageResponse response = passwordRecoveryService.restablecerPassword(
                    "test@mail.com", "123456", "NuevaPass123"
            );

            assertThat(response.successCode()).isEqualTo("PASSWORD_UPDATED");
            verify(redisOtpService).validarCodigo("test@mail.com", "123456", VerificationCodeType.RECUPERACION);
            verify(redisOtpService).eliminarCodigo("test@mail.com", VerificationCodeType.RECUPERACION);
            verify(usuarioRepository).save(any(User.class));
            verify(eventPublisher).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe lanzar USER_NOT_FOUND si usuario no existe")
        void debeLanzarErrorSiUsuarioNoExiste() {
            when(usuarioRepository.findByEmailAndEstadoTrue("noexiste@mail.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordRecoveryService.restablecerPassword(
                    "noexiste@mail.com", "123456", "NuevaPass123"
            ))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("USER_NOT_FOUND");
                        assertThat(appEx.getStatus().value()).isEqualTo(404);
                    });
        }
    }
}
