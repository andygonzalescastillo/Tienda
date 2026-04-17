package com.tienda.backend.events;

import com.tienda.backend.domain.enums.VerificationCodeType;
import com.tienda.backend.service.email.EmailService;
import com.tienda.backend.service.redis.RedisOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final RedisOtpService redisOtpService;
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            redisOtpService.generarCodigo(event.usuario().getEmail(), VerificationCodeType.REGISTRO);
        } catch (Exception e) {
            log.error("Error generando OTP para registro de {}: {}", event.usuario().getEmail(), e.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleSendOtp(SendOtpEvent event) {
        try {
            redisOtpService.generarCodigo(event.email(), event.type());
        } catch (Exception e) {
            log.error("Error generando OTP para {} ({}): {}", event.email(), event.type(), e.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleOtpGenerated(OtpGeneratedEvent event) {
        try {
            switch (event.type()) {
                case REGISTRO -> emailService.enviarCodigoVerificacion(event.email(), event.codigo(), event.expirationMinutes());
                case RECUPERACION -> emailService.enviarCodigoRecuperacion(event.email(), event.codigo(), event.expirationMinutes());
            }
        } catch (Exception e) {
            log.error("Error enviando OTP a {} ({}): {}", event.email(), event.type(), e.getMessage());
        }
    }
}