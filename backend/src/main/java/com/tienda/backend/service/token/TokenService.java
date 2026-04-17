package com.tienda.backend.service.token;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.SessionResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.events.SessionCreatedEvent;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.websocket.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtils jwtUtilidad;
    private final UserRepository usuarioRepo;
    private final TokenBlacklistService blacklistService;
    private final SessionService sessionService;
    private final SessionActivityService sessionActivityService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public LoginResult generarParDeTokens(User usuario, String ipAddress, String userAgent) {
        var refresh = jwtUtilidad.generarRefreshToken(usuario.getEmail(), usuario.getId(), usuario.getRol().name());
        var refreshJti = jwtUtilidad.obtenerJti(refresh);

        var access = jwtUtilidad.generarAccessToken(
                usuario.getEmail(),
                usuario.getId(),
                usuario.getRol().name(),
                refreshJti
        );

        sessionService.guardarRefreshToken(refresh, usuario, ipAddress, userAgent);
        eventPublisher.publishEvent(new SessionCreatedEvent(usuario.getEmail()));

        return new LoginResult(
                access,
                refresh,
                "LOGIN_SUCCESS",
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                usuario.getRol().name()
        );
    }

    @Transactional
    public LoginResult refrescarToken(String refreshToken, String ipAddress, String userAgent) {
        if (!jwtUtilidad.esTokenValido(refreshToken)) {
            throw AppException.unauthorized("INVALID_REFRESH_TOKEN");
        }
        if (!jwtUtilidad.esRefreshToken(refreshToken)) {
            throw AppException.unauthorized("INVALID_TOKEN_TYPE");
        }

        var jti = jwtUtilidad.obtenerJti(refreshToken);
        var tokenEntity = sessionService.buscarOpcionalPorTokenId(jti)
                .orElseThrow(() -> AppException.unauthorized("REFRESH_TOKEN_NOT_FOUND"));

        var user = tokenEntity.getUser();
        var email = user.getEmail();
        var rol = user.getRol().name();

        var nuevoRefresh = jwtUtilidad.generarRefreshToken(email, user.getId(), rol);
        var nuevoAccess = jwtUtilidad.generarAccessToken(email, user.getId(), rol, jwtUtilidad.obtenerJti(nuevoRefresh));

        sessionActivityService.removerDeCache(jti);

        sessionService.actualizarRefreshToken(tokenEntity, nuevoRefresh);

        return new LoginResult(nuevoAccess, nuevoRefresh, "TOKEN_REFRESH_SUCCESS", email, null, rol);
    }

    @Transactional
    public void cerrarSesion(String accessToken, String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            revocarRefreshToken(refreshToken, "LOGOUT");
        }
        if (accessToken != null && !accessToken.isBlank()) {
            revocarAccessToken(accessToken, "LOGOUT");
        }
    }

    @Transactional
    public void cerrarTodasLasSesionesDelUsuario(String email) {
        var user = usuarioRepo.findByEmailAndEstadoTrue(email)
                .orElseThrow(() -> AppException.notFound("USER_NOT_FOUND", Map.of("email", email)));

        revocarTodosLosTokensDelUsuario(user, "LOGOUT_ALL");

        notificationService.enviarAUsuario(email, NotificationService.SESSION_EVENTS_QUEUE,
                WebSocketMessageDto.of("FORCE_LOGOUT"));
    }

    @Transactional
    public MessageResponse cerrarSesionEspecifica(String targetTokenId, String email, String currentRefreshTokenJti) {
        if (targetTokenId.equals(currentRefreshTokenJti)) {
            throw AppException.badRequest("CANNOT_CLOSE_CURRENT_SESSION", Map.of());
        }

        var user = usuarioRepo.findByEmailAndEstadoTrue(email)
                .orElseThrow(() -> AppException.notFound("USER_NOT_FOUND", Map.of("email", email)));

        var tokenTarget = sessionService.buscarPorTokenId(targetTokenId);

        if (!tokenTarget.getUser().getId().equals(user.getId())) {
            throw AppException.forbidden("SESSION_ACCESS_DENIED");
        }

        sessionService.revocarRefreshTokenEntity(tokenTarget, "REVOCACION_MANUAL_USUARIO");

        notificationService.enviarAUsuario(email, NotificationService.SESSION_EVENTS_QUEUE,
                WebSocketMessageDto.of("SESSION_REVOKED", targetTokenId));

        return new MessageResponse("SESSION_CLOSED");
    }

    public void revocarAccessToken(String token, String razon) {
        if (jwtUtilidad.esTokenValido(token) && jwtUtilidad.esAccessToken(token)) {
            long ttl = Duration.between(Instant.now(), jwtUtilidad.obtenerFechaExpiracion(token).toInstant()).toSeconds();
            if (ttl > 0) {
                blacklistService.agregarAListaNegra(jwtUtilidad.obtenerJti(token), ttl);
            }
        }
    }

    @Transactional
    public void revocarRefreshToken(String token, String razon) {
        if (jwtUtilidad.esTokenValido(token) && jwtUtilidad.esRefreshToken(token)) {
            String jti = jwtUtilidad.obtenerJti(token);
            sessionActivityService.removerDeCache(jti);
            
            sessionService.buscarOpcionalPorTokenId(jti)
                    .ifPresent(entity -> {
                        sessionService.revocarRefreshTokenEntity(entity, razon);
                        if ("LOGOUT".equals(razon)) {
                            notificationService.enviarAUsuario(
                                entity.getUser().getEmail(), 
                                NotificationService.SESSION_EVENTS_QUEUE,
                                WebSocketMessageDto.of("SESSIONS_UPDATED")
                            );
                        }
                    });
        }
    }

    @Transactional
    public void revocarTodosLosTokensDelUsuario(User usuario, String razon) {
        sessionService.revocarTodosPorUsuario(usuario.getId(), razon);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> obtenerSesionesActivas(String email, String currentRefreshTokenJti) {
        var user = usuarioRepo.findByEmailAndEstadoTrue(email)
                .orElseThrow(() -> AppException.notFound("USER_NOT_FOUND", Map.of("email", email)));
        return sessionService.obtenerSesionesActivas(user.getId(), currentRefreshTokenJti);
    }

    public String obtenerJtiRefreshToken(String refreshToken) {
        if (refreshToken == null || !jwtUtilidad.esTokenValido(refreshToken)) {
            return null;
        }
        return jwtUtilidad.obtenerJti(refreshToken);
    }
}