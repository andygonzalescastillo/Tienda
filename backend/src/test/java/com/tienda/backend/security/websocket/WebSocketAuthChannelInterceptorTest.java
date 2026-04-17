package com.tienda.backend.security.websocket;

import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.token.TokenValidationService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAuthChannelInterceptor")
class WebSocketAuthChannelInterceptorTest {

    @Mock private JwtUtils jwtUtils;
    @Mock private TokenValidationService tokenValidationService;
    @Mock private MessageChannel channel;
    @Mock private Claims claims;

    @InjectMocks
    private WebSocketAuthChannelInterceptor interceptor;

    private org.springframework.messaging.Message<?> crearMensajeConnect(Cookie[] cookies) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        var sessionAttrs = new HashMap<String, Object>();
        if (cookies != null) {
            sessionAttrs.put(WebSocketHandshakeInterceptor.COOKIES_ATTR, cookies);
        }
        accessor.setSessionAttributes(sessionAttrs);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Nested
    @DisplayName("CONNECT con token válido")
    class ConnectValido {

        @Test
        @DisplayName("Debe autenticar usuario con token válido desde cookies")
        void debeAutenticarConTokenValido() {
            var cookies = new Cookie[]{new Cookie("access_token", "valid-jwt")};
            var message = crearMensajeConnect(cookies);

            when(jwtUtils.esTokenValido("valid-jwt")).thenReturn(true);
            when(jwtUtils.obtenerJti("valid-jwt")).thenReturn("jti-1");
            when(jwtUtils.obtenerRefreshTokenJti("valid-jwt")).thenReturn("refresh-jti-1");
            when(tokenValidationService.estaRevocado("jti-1")).thenReturn(false);
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti-1")).thenReturn(false);
            when(jwtUtils.parsearClaims("valid-jwt")).thenReturn(claims);
            when(claims.getSubject()).thenReturn("user@mail.com");
            when(claims.get("rol", String.class)).thenReturn("USER");

            var result = interceptor.preSend(message, channel);

            assertThat(result).isNotNull();
            var accessor = StompHeaderAccessor.wrap(result);
            var principal = accessor.getUser();
            assertThat(principal).isNotNull();
            assertThat(principal.getName()).isEqualTo("user@mail.com");
            assertThat(principal).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        }
    }

    @Nested
    @DisplayName("CONNECT con token inválido")
    class ConnectInvalido {

        @Test
        @DisplayName("Debe lanzar excepción si no hay cookies")
        void debeLanzarExcepcionSiNoCookies() {
            var message = crearMensajeConnect(null);

            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("INVALID_WS_TOKEN");
        }

        @Test
        @DisplayName("Debe lanzar excepción si no hay access_token en cookies")
        void debeLanzarExcepcionSiNoAccessToken() {
            var cookies = new Cookie[]{new Cookie("other_cookie", "value")};
            var message = crearMensajeConnect(cookies);

            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("INVALID_WS_TOKEN");
        }

        @Test
        @DisplayName("Debe lanzar excepción si token JWT es inválido")
        void debeLanzarExcepcionSiTokenInvalido() {
            var cookies = new Cookie[]{new Cookie("access_token", "bad-jwt")};
            var message = crearMensajeConnect(cookies);

            when(jwtUtils.esTokenValido("bad-jwt")).thenReturn(false);

            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("INVALID_WS_TOKEN");
        }

        @Test
        @DisplayName("Debe lanzar excepción si access token está revocado")
        void debeLanzarExcepcionSiTokenRevocado() {
            var cookies = new Cookie[]{new Cookie("access_token", "revoked-jwt")};
            var message = crearMensajeConnect(cookies);

            when(jwtUtils.esTokenValido("revoked-jwt")).thenReturn(true);
            when(jwtUtils.obtenerJti("revoked-jwt")).thenReturn("jti-revoked");
            when(tokenValidationService.estaRevocado("jti-revoked")).thenReturn(true);

            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("REVOKED_WS_TOKEN");
        }

        @Test
        @DisplayName("Debe lanzar excepción si sesión (refresh token) está revocada")
        void debeLanzarExcepcionSiSesionRevocada() {
            var cookies = new Cookie[]{new Cookie("access_token", "valid-jwt")};
            var message = crearMensajeConnect(cookies);

            when(jwtUtils.esTokenValido("valid-jwt")).thenReturn(true);
            when(jwtUtils.obtenerJti("valid-jwt")).thenReturn("jti-ok");
            when(jwtUtils.obtenerRefreshTokenJti("valid-jwt")).thenReturn("refresh-revoked");
            when(tokenValidationService.estaRevocado("jti-ok")).thenReturn(false);
            when(tokenValidationService.esRefreshTokenRevocado("refresh-revoked")).thenReturn(true);

            assertThatThrownBy(() -> interceptor.preSend(message, channel))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SESSION_REVOKED");
        }
    }

    @Nested
    @DisplayName("Mensajes no-CONNECT")
    class NoConnect {

        @Test
        @DisplayName("Debe pasar mensajes SEND sin validar token")
        void debeDejarPasarMensajesSend() {
            var accessor = StompHeaderAccessor.create(StompCommand.SEND);
            accessor.setSessionAttributes(new HashMap<>());
            var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            var result = interceptor.preSend(message, channel);

            assertThat(result).isNotNull();
            verifyNoInteractions(jwtUtils, tokenValidationService);
        }
    }
}
