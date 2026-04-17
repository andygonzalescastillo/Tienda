package com.tienda.backend.service.redis;

import com.tienda.backend.config.properties.SecurityProperties;
import com.tienda.backend.domain.enums.VerificationCodeType;
import com.tienda.backend.events.OtpGeneratedEvent;
import com.tienda.backend.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RedisOtpService {

    private final ApplicationEventPublisher eventPublisher;
    private final SecurityProperties securityProps;
    private final StringRedisTemplate redisTemplate;

    public void guardarCodigo(String email, String codigo, VerificationCodeType tipo) {
        var key = generarKey(email, tipo);
        redisTemplate.opsForHash().putAll(key, Map.of("codigo", codigo, "intentos", "0"));
        redisTemplate.expire(key, Duration.ofMinutes(securityProps.otp().expirationMinutes()));
    }

    public void validarCodigo(String email, String codigoUsuario, VerificationCodeType tipo) {
        validarCodigo(email, codigoUsuario, tipo, true);
    }

    public void validarCodigo(String email, String codigoUsuario, VerificationCodeType tipo, boolean eliminarAlValidar) {
        var key = generarKey(email, tipo);

        var codigoGuardado = (String) redisTemplate.opsForHash().get(key, "codigo");

        if (codigoGuardado == null) {
            long minutos = securityProps.otp().expirationMinutes();
            throw AppException.badRequest("VERIFICATION_CODE_EXPIRED", Map.of("expirationMinutes", minutos));
        }

        if (Objects.equals(codigoGuardado, codigoUsuario)) {
            if (eliminarAlValidar) {
                redisTemplate.delete(key);
            }
            return;
        }

        Long intentosActuales = redisTemplate.opsForHash().increment(key, "intentos", 1);
        int maxAttempts = securityProps.otp().maxAttempts();

        if (intentosActuales != null && intentosActuales >= maxAttempts) {
            redisTemplate.delete(key);
            throw AppException.badRequest(
                    "MAX_ATTEMPTS_EXCEEDED",
                    Map.of("maxAttempts", maxAttempts)
            );
        }

        throw AppException.badRequest(
                "INVALID_VERIFICATION_CODE",
                Map.of("remainingAttempts", maxAttempts - (intentosActuales != null ? intentosActuales : 0))
        );
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    public void generarCodigo(String email, VerificationCodeType tipo) {
        eliminarCodigo(email, tipo);
        var codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        guardarCodigo(email, codigo, tipo);
        long minutos = securityProps.otp().expirationMinutes();
        eventPublisher.publishEvent(new OtpGeneratedEvent(email, codigo, tipo, minutos));
    }

    public void eliminarCodigo(String email, VerificationCodeType tipo) {
        redisTemplate.delete(generarKey(email, tipo));
    }

    private String generarKey(String email, VerificationCodeType tipo) {
        return "OTP:" + email + ":" + tipo.name();
    }
}