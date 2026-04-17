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
import com.tienda.backend.events.SendOtpEvent;
import com.tienda.backend.events.UserRegisteredEvent;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.service.redis.RedisOtpService;
import com.tienda.backend.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisOtpService redisOtpService;
    private final SecurityProperties securityProperties;

    @Transactional(readOnly = true)
    public VerifyEmailExistenceResponse verificarEmailExistente(String email) {
        return usuarioRepository.findByEmailAndEstadoTrue(email)
                .filter(User::getEmailVerificado)
                .map(u -> {
                    var proveedores = u.getProviders().stream().map(p -> p.getProvider().name()).toList();
                    var proveedorPral = u.hasProvider(AuthProvider.LOCAL) ? "LOCAL" : (proveedores.isEmpty() ? "UNKNOWN" : proveedores.getFirst());
                    return new VerifyEmailExistenceResponse(true, StringUtils.hasText(u.getPassword()), proveedorPral, proveedores);
                })
                .orElse(new VerifyEmailExistenceResponse(false, false, null, null));
    }

    @Transactional
    public RegisterResponse registrarUsuarioSinVerificar(RegisterRequest request) {
        var email = request.email();
        var usuario = usuarioRepository.findByEmail(email).orElseGet(User::new);

        if (usuario.getId() != null && usuario.getEmailVerificado()) {
            throw AppException.conflict("EMAIL_ALREADY_VERIFIED");
        }

        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());

        if (usuario.getId() == null) {
            usuario.setRol(UserRole.USER);
            usuario.setEmailVerificado(false);
            usuario.getAudit().setUsuarioRegistro(email);
            usuario.setEstado(true);
        }

        if (!usuario.hasProvider(AuthProvider.LOCAL)) {
            usuario.addProvider(AuthProvider.LOCAL, "LOCAL");
        }

        User usuarioGuardado = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UserRegisteredEvent(usuarioGuardado));

        long minutos = securityProperties.otp().expirationMinutes();

        return new RegisterResponse(
                email,
                usuarioGuardado.getNombreCompleto(),
                Map.of("expirationMinutes", minutos)
        );
    }

    @Transactional
    public LoginResult verificarEmailYGenerarTokens(VerifyCodeRequest request, String ipAddress, String userAgent) {
        var email = request.email();

        redisOtpService.validarCodigo(email, request.codigo(), VerificationCodeType.REGISTRO);

        var usuario = usuarioRepository.findByEmailAndEstadoTrue(email)
                .orElseThrow(() -> AppException.notFound("USER_NOT_FOUND", Map.of("entity", "User", "identifier", email)));

        usuario.setEmailVerificado(true);
        usuario.setUltimaSesion(Instant.now());
        usuario.getAudit().setUsuarioUltimaModificacion("SISTEMA");
        usuarioRepository.save(usuario);

        redisOtpService.eliminarCodigo(email, VerificationCodeType.REGISTRO);

        return tokenService.generarParDeTokens(usuario, ipAddress, userAgent);
    }

    @Transactional(readOnly = true)
    public MessageResponse reenviarCodigo(String email) {
        usuarioRepository.findByEmail(email)
                .filter(u -> !u.getEmailVerificado())
                .orElseThrow(() -> AppException.conflict("EMAIL_ERROR"));

        eventPublisher.publishEvent(new SendOtpEvent(email, VerificationCodeType.REGISTRO));
        
        long minutos = securityProperties.otp().expirationMinutes();
        return new MessageResponse("CODE_RESENT", Map.of("expirationMinutes", minutos));
    }
}