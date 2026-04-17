package com.tienda.backend.service.util;

import com.tienda.backend.config.properties.CookieProperties;
import com.tienda.backend.config.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CookieService {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProps;

    public void agregarAccessTokenCookie(HttpServletResponse response, String token) {
        long maxAgeSeconds = jwtProperties.accessToken().expiration() / 1000;
        agregarCookie(response, ACCESS_TOKEN_COOKIE, token, Duration.ofSeconds(maxAgeSeconds));
    }

    public void agregarRefreshTokenCookie(HttpServletResponse response, String token) {
        long maxAgeSeconds = jwtProperties.refreshToken().expiration() / 1000;
        agregarCookie(response, REFRESH_TOKEN_COOKIE, token, Duration.ofSeconds(maxAgeSeconds));
    }

    public void eliminarCookies(HttpServletResponse response) {
        agregarCookie(response, ACCESS_TOKEN_COOKIE, "", Duration.ZERO);
        agregarCookie(response, REFRESH_TOKEN_COOKIE, "", Duration.ZERO);
    }

    public Optional<String> obtenerAccessToken(HttpServletRequest request) {
        return obtenerCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> obtenerRefreshToken(HttpServletRequest request) {
        return obtenerCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    private void agregarCookie(HttpServletResponse response, String nombre, String valor, Duration maxAge) {
        var builder = ResponseCookie.from(nombre, valor)
                .httpOnly(true)
                .secure(cookieProps.secure())
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieProps.secure() ? "None" : "Lax");

        if (StringUtils.hasText(cookieProps.domain()) && !"localhost".equalsIgnoreCase(cookieProps.domain())) {
            builder.domain(cookieProps.domain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private Optional<String> obtenerCookieValue(HttpServletRequest request, String nombre) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> nombre.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst();
    }
}