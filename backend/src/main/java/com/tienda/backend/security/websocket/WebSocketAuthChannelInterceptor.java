package com.tienda.backend.security.websocket;

import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.token.TokenValidationService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final TokenValidationService tokenValidationService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractTokenFromCookies(accessor);

            if (token == null || !jwtUtils.esTokenValido(token)) {
                throw new IllegalArgumentException("INVALID_WS_TOKEN");
            }

            String jti = jwtUtils.obtenerJti(token);
            if (tokenValidationService.estaRevocado(jti)) {
                throw new IllegalArgumentException("REVOKED_WS_TOKEN");
            }

            String refreshTokenJti = jwtUtils.obtenerRefreshTokenJti(token);
            if (tokenValidationService.esRefreshTokenRevocado(refreshTokenJti)) {
                throw new IllegalArgumentException("SESSION_REVOKED");
            }

            var claims = jwtUtils.parsearClaims(token);
            String email = claims.getSubject();
            String rol = claims.get("rol", String.class);

            Principal principal = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(() -> "ROLE_" + rol)
            );
            accessor.setUser(principal);
        }

        return message;
    }

    private String extractTokenFromCookies(StompHeaderAccessor accessor) {
        var sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object cookiesObj = sessionAttributes.get(WebSocketHandshakeInterceptor.COOKIES_ATTR);
            if (cookiesObj instanceof Cookie[] cookies) {
                String token = Arrays.stream(cookies)
                        .filter(c -> "access_token".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
                if (token != null) return token;
            }
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
