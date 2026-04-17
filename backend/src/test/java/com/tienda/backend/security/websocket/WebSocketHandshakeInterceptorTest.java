package com.tienda.backend.security.websocket;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketHandshakeInterceptor")
class WebSocketHandshakeInterceptorTest {

    @Mock private ServerHttpResponse response;
    @Mock private WebSocketHandler wsHandler;

    @InjectMocks
    private WebSocketHandshakeInterceptor interceptor;

    @Test
    @DisplayName("Debe extraer cookies del request y agregarlas a los atributos")
    void debeExtraerCookiesYAgregarAAtributos() {
        var httpRequest = mock(HttpServletRequest.class);
        var servletRequest = mock(ServletServerHttpRequest.class);
        var cookies = new Cookie[]{new Cookie("access_token", "jwt-123"), new Cookie("refresh_token", "rt-456")};

        when(servletRequest.getServletRequest()).thenReturn(httpRequest);
        when(httpRequest.getCookies()).thenReturn(cookies);

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes).containsKey(WebSocketHandshakeInterceptor.COOKIES_ATTR);
        var storedCookies = (Cookie[]) attributes.get(WebSocketHandshakeInterceptor.COOKIES_ATTR);
        assertThat(storedCookies).hasSize(2);
        assertThat(storedCookies[0].getName()).isEqualTo("access_token");
    }

    @Test
    @DisplayName("Debe retornar true sin agregar cookies si request no tiene cookies")
    void debeRetornarTrueSinCookiesSiNoHay() {
        var httpRequest = mock(HttpServletRequest.class);
        var servletRequest = mock(ServletServerHttpRequest.class);

        when(servletRequest.getServletRequest()).thenReturn(httpRequest);
        when(httpRequest.getCookies()).thenReturn(null);

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes).doesNotContainKey(WebSocketHandshakeInterceptor.COOKIES_ATTR);
    }

    @Test
    @DisplayName("Debe retornar true si request no es ServletServerHttpRequest")
    void debeRetornarTrueSiNoEsServletRequest() {
        var genericRequest = mock(org.springframework.http.server.ServerHttpRequest.class);

        Map<String, Object> attributes = new HashMap<>();
        boolean result = interceptor.beforeHandshake(genericRequest, response, wsHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes).isEmpty();
    }

    @Test
    @DisplayName("afterHandshake no debe lanzar excepción")
    void afterHandshakeNoDebeLanzarExcepcion() {
        var request = mock(org.springframework.http.server.ServerHttpRequest.class);

        interceptor.afterHandshake(request, response, wsHandler, null);
        interceptor.afterHandshake(request, response, wsHandler, new RuntimeException("test"));
    }
}
