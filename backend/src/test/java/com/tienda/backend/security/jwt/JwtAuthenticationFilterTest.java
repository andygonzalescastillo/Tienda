package com.tienda.backend.security.jwt;

import com.tienda.backend.service.token.SessionActivityService;
import com.tienda.backend.service.token.TokenValidationService;
import com.tienda.backend.service.util.CookieService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock private JwtUtils jwtUtils;
    @Mock private TokenValidationService tokenValidationService;
    @Mock private SessionActivityService sessionActivityService;
    @Mock private CookieService cookieService;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private Claims claims;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Request sin token")
    class SinToken {

        @Test
        @DisplayName("Debe continuar filter chain sin autenticar si no hay token")
        void debeContinuarSinAutenticarSiNoHayToken() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.empty());
            when(request.getHeader("Authorization")).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("Token válido")
    class TokenValido {

        @Test
        @DisplayName("Debe autenticar usuario con token válido desde cookie")
        void debeAutenticarConTokenDesdeCookie() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.of("valid-jwt"));
            when(jwtUtils.parsearClaims("valid-jwt")).thenReturn(claims);
            when(claims.getId()).thenReturn("jti-123");
            when(claims.getSubject()).thenReturn("user@mail.com");
            when(claims.get("type", String.class)).thenReturn("access");
            when(claims.get("rol", String.class)).thenReturn("USER");
            when(claims.get("refreshTokenJti", String.class)).thenReturn("refresh-jti");
            when(tokenValidationService.estaRevocado("jti-123")).thenReturn(false);
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti")).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("user@mail.com");
            assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("Debe autenticar con token desde header Authorization Bearer")
        void debeAutenticarConTokenDesdeHeader() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.empty());
            when(request.getHeader("Authorization")).thenReturn("Bearer header-jwt-token");
            when(jwtUtils.parsearClaims("header-jwt-token")).thenReturn(claims);
            when(claims.getId()).thenReturn("jti-header");
            when(claims.getSubject()).thenReturn("admin@mail.com");
            when(claims.get("type", String.class)).thenReturn("access");
            when(claims.get("rol", String.class)).thenReturn("ADMIN");
            when(claims.get("refreshTokenJti", String.class)).thenReturn(null);
            when(tokenValidationService.estaRevocado("jti-header")).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("admin@mail.com");
            assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Debe registrar actividad de sesión si refreshTokenJti presente")
        void debeRegistrarActividadDeSesion() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.of("valid-jwt"));
            when(jwtUtils.parsearClaims("valid-jwt")).thenReturn(claims);
            when(claims.getId()).thenReturn("jti-1");
            when(claims.getSubject()).thenReturn("user@mail.com");
            when(claims.get("type", String.class)).thenReturn("access");
            when(claims.get("rol", String.class)).thenReturn("USER");
            when(claims.get("refreshTokenJti", String.class)).thenReturn("refresh-jti-abc");
            when(tokenValidationService.estaRevocado("jti-1")).thenReturn(false);
            when(tokenValidationService.esRefreshTokenRevocado("refresh-jti-abc")).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(sessionActivityService).registrarActividad("refresh-jti-abc");
        }
    }

    @Nested
    @DisplayName("Token revocado")
    class TokenRevocado {

        @Test
        @DisplayName("Debe limpiar contexto y cookies si access token está revocado")
        void debeRechazarAccessTokenRevocado() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.of("revoked-jwt"));
            when(jwtUtils.parsearClaims("revoked-jwt")).thenReturn(claims);
            when(claims.getId()).thenReturn("jti-revoked");
            when(tokenValidationService.estaRevocado("jti-revoked")).thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(cookieService).eliminarCookies(response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Debe limpiar contexto y cookies si refresh token de la sesión está revocado")
        void debeRechazarSiRefreshTokenRevocado() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.of("valid-jwt"));
            when(jwtUtils.parsearClaims("valid-jwt")).thenReturn(claims);
            when(claims.getId()).thenReturn("jti-ok");
            when(claims.get("refreshTokenJti", String.class)).thenReturn("refresh-revoked");
            when(tokenValidationService.estaRevocado("jti-ok")).thenReturn(false);
            when(tokenValidationService.esRefreshTokenRevocado("refresh-revoked")).thenReturn(true);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(cookieService).eliminarCookies(response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("Token inválido")
    class TokenInvalido {

        @Test
        @DisplayName("Debe limpiar contexto y cookies si token no es tipo 'access'")
        void debeRechazarTokenConTipoInvalido() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.of("refresh-jwt"));
            when(jwtUtils.parsearClaims("refresh-jwt")).thenReturn(claims);
            when(claims.getId()).thenReturn("jti-refresh");
            when(claims.get("refreshTokenJti", String.class)).thenReturn(null);
            when(claims.get("type", String.class)).thenReturn("refresh");
            when(tokenValidationService.estaRevocado("jti-refresh")).thenReturn(false);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(cookieService).eliminarCookies(response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Debe manejar JwtException limpiando contexto y cookies")
        void debeManejarJwtException() throws Exception {
            when(cookieService.obtenerAccessToken(request)).thenReturn(Optional.of("malformed-jwt"));
            when(jwtUtils.parsearClaims("malformed-jwt")).thenThrow(new JwtException("Token malformado"));

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(cookieService).eliminarCookies(response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
