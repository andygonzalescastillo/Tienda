package com.tienda.backend.service.auth;

import com.tienda.backend.config.properties.SecurityProperties;
import com.tienda.backend.domain.enums.VerificationCodeType;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.events.SendOtpEvent;
import com.tienda.backend.events.UserPasswordChangedEvent;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.service.redis.RedisOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisOtpService redisOtpService;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityProperties securityProperties;

    @Transactional(readOnly = true)
    public MessageResponse solicitarRecuperacionPassword(String email) {
        long minutos = securityProperties.otp().expirationMinutes();
        var metadata = Map.<String, Object>of("expirationMinutes", minutos);

        var usuarioOpt = usuarioRepository.findByEmailAndEstadoTrue(email);

        if (usuarioOpt.isEmpty()) {
            return new MessageResponse("OTP_SENT", metadata);
        }

        var usuario = usuarioOpt.get();

        if (!StringUtils.hasText(usuario.getPassword())) {
            return new MessageResponse("OTP_SENT", metadata);
        }

        if (!usuario.getEmailVerificado()) {
            return new MessageResponse("OTP_SENT", metadata);
        }

        eventPublisher.publishEvent(new SendOtpEvent(email, VerificationCodeType.RECUPERACION));

        return new MessageResponse("OTP_SENT", metadata);
    }

    public MessageResponse verificarCodigoRecuperacion(String email, String codigo) {
        redisOtpService.validarCodigo(email, codigo, VerificationCodeType.RECUPERACION, false);
        return new MessageResponse("CODE_VERIFIED");
    }

    @Transactional
    public MessageResponse restablecerPassword(String email, String codigo, String nuevaPassword) {
        redisOtpService.validarCodigo(email, codigo, VerificationCodeType.RECUPERACION);

        var usuario = usuarioRepository.findByEmailAndEstadoTrue(email).orElseThrow(() -> AppException.notFound("USER_NOT_FOUND"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        
        redisOtpService.eliminarCodigo(email, VerificationCodeType.RECUPERACION);
        eventPublisher.publishEvent(new UserPasswordChangedEvent(usuario));

        return new MessageResponse("PASSWORD_UPDATED");
    }
}