package com.tienda.backend.service.auth;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.dto.auth.request.LoginRequest;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.SessionValidationResponse;
import com.tienda.backend.events.UserAuthenticatedEvent;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.token.TokenValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final TokenService tokenService;
    private final TokenValidationService tokenValidationService;
    private final JwtUtils jwtUtilidad;

    @Transactional
    public LoginResult autenticarUsuario(LoginRequest request, String ipAddress, String userAgent) {
        var email = request.email();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException e) {
            throw AppException.unauthorized("INVALID_CREDENTIALS");
        }

        var usuario = usuarioRepository.findByEmailAndEstadoTrue(email)
                .orElseThrow(() -> AppException.unauthorized("INVALID_SESSION"));

        eventPublisher.publishEvent(new UserAuthenticatedEvent(usuario.getId()));
        return tokenService.generarParDeTokens(usuario, ipAddress, userAgent);
    }

    public SessionValidationResponse validarSesionActual(String email, String accessToken) {
        var refreshTokenJti = jwtUtilidad.obtenerRefreshTokenJti(accessToken);
        if (tokenValidationService.esRefreshTokenRevocado(refreshTokenJti)) {
            throw AppException.unauthorized("SESSION_REVOKED");
        }

        var usuario = usuarioRepository.findByEmailAndEstadoTrue(email.strip().toLowerCase())
                .filter(User::getEmailVerificado)
                .orElseThrow(() -> AppException.unauthorized("INVALID_SESSION"));

        return new SessionValidationResponse(
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                usuario.getRol().name()
        );
    }
}