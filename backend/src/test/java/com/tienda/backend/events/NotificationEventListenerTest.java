package com.tienda.backend.events;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.VerificationCodeType;
import com.tienda.backend.service.email.EmailService;
import com.tienda.backend.service.redis.RedisOtpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener - Manejo de eventos de OTP y Notificación")
class NotificationEventListenerTest {

    @Mock private RedisOtpService redisOtpService;
    @Mock private EmailService emailService;

    @InjectMocks
    private NotificationEventListener listener;

    @Nested
    @DisplayName("handleUserRegistered")
    class HandleUserRegistered {

        @Test
        @DisplayName("Debe solicitar generación de OTP de registro al servicio Redis")
        void debeSolicitarGeneracionDeOtp() {
            User user = User.builder().email("test@mail.com").build();
            UserRegisteredEvent event = new UserRegisteredEvent(user);

            listener.handleUserRegistered(event);

            verify(redisOtpService).generarCodigo("test@mail.com", VerificationCodeType.REGISTRO);
        }

        @Test
        @DisplayName("Debe capturar excepciones sin propagar para no afectar transacciones")
        void debeCapturarExcepciones() {
            User user = User.builder().email("test@mail.com").build();
            UserRegisteredEvent event = new UserRegisteredEvent(user);
            doThrow(new RuntimeException("Redis error")).when(redisOtpService).generarCodigo(anyString(), any());

            assertThatCode(() -> listener.handleUserRegistered(event))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("handleSendOtp")
    class HandleSendOtp {

        @Test
        @DisplayName("Debe solicitar generación de OTP con el tipo especificado")
        void debeSolicitarGeneracionDeOtp() {
            SendOtpEvent event = new SendOtpEvent("user@mail.com", VerificationCodeType.RECUPERACION);

            listener.handleSendOtp(event);

            verify(redisOtpService).generarCodigo("user@mail.com", VerificationCodeType.RECUPERACION);
        }

        @Test
        @DisplayName("Debe capturar excepciones sin propagar")
        void debeCapturarExcepciones() {
            SendOtpEvent event = new SendOtpEvent("user@mail.com", VerificationCodeType.RECUPERACION);
            doThrow(new RuntimeException("Error")).when(redisOtpService).generarCodigo(anyString(), any());

            assertThatCode(() -> listener.handleSendOtp(event))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("handleOtpGenerated")
    class HandleOtpGenerated {

        @Test
        @DisplayName("Debe enviar correo de verificación si el tipo es REGISTRO")
        void debeEnviarVerificacionSiRegistro() {
            OtpGeneratedEvent event = new OtpGeneratedEvent("test@mail.com", "123456", VerificationCodeType.REGISTRO, 15);

            listener.handleOtpGenerated(event);

            verify(emailService).enviarCodigoVerificacion("test@mail.com", "123456", 15);
            verifyNoMoreInteractions(emailService);
        }

        @Test
        @DisplayName("Debe enviar correo de recuperación si el tipo es RECUPERACION")
        void debeEnviarRecuperacionSiRecuperacion() {
            OtpGeneratedEvent event = new OtpGeneratedEvent("test@mail.com", "654321", VerificationCodeType.RECUPERACION, 10);

            listener.handleOtpGenerated(event);

            verify(emailService).enviarCodigoRecuperacion("test@mail.com", "654321", 10);
            verifyNoMoreInteractions(emailService);
        }

        @Test
        @DisplayName("Debe capturar excepciones sin propagar")
        void debeCapturarExcepciones() {
            OtpGeneratedEvent event = new OtpGeneratedEvent("test@mail.com", "123456", VerificationCodeType.REGISTRO, 15);
            doThrow(new RuntimeException("Mail error")).when(emailService).enviarCodigoVerificacion(anyString(), anyString(), anyLong());

            assertThatCode(() -> listener.handleOtpGenerated(event))
                    .doesNotThrowAnyException();
        }
    }
}
