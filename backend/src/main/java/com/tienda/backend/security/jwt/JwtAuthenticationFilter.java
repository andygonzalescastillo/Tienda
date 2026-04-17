package com.tienda.backend.security.jwt;

import com.tienda.backend.service.token.SessionActivityService;
import com.tienda.backend.service.token.TokenValidationService;
import com.tienda.backend.service.util.CookieService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenValidationService tokenValidationService;
    private final SessionActivityService sessionActivityService;
    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Optional<String> tokenOpt = extraerToken(request);

            if (tokenOpt.isPresent()) {
                String token = tokenOpt.get();

                Claims claims = jwtUtils.parsearClaims(token);
                String jti = claims.getId();

                if (esTokenRevocado(jti)) {
                    limpiarContexto(response);
                    filterChain.doFilter(request, response);
                    return;
                }

                String refreshTokenId = claims.get("refreshTokenJti", String.class);
                if (refreshTokenId != null && esRefreshTokenRevocado(refreshTokenId)) {
                    limpiarContexto(response);
                    filterChain.doFilter(request, response);
                    return;
                }

                String tipo = claims.get("type", String.class);
                if (!"access".equals(tipo)) {
                    limpiarContexto(response);
                    filterChain.doFilter(request, response);
                    return;
                }

                if (refreshTokenId != null) {
                    try {
                        sessionActivityService.registrarActividad(refreshTokenId);
                    } catch (Exception e) {
                        log.warn("Error registrando actividad de sesión: {}", e.getMessage());
                    }
                }

                String email = claims.getSubject();
                String rol = claims.get("rol", String.class);

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException e) {
            log.debug("Token JWT inválido: {}", e.getMessage());
            limpiarContexto(response);
        } catch (Exception e) {
            log.error("Error inesperado en filtro JWT (NO se eliminan cookies): {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private boolean esTokenRevocado(String jti) {
        try {
            return tokenValidationService.estaRevocado(jti);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean esRefreshTokenRevocado(String refreshTokenJti) {
        try {
            return tokenValidationService.esRefreshTokenRevocado(refreshTokenJti);
        } catch (Exception e) {
            return false; 
        }
    }

    private void limpiarContexto(HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        cookieService.eliminarCookies(response);
    }

    private Optional<String> extraerToken(HttpServletRequest request) {
        return cookieService.obtenerAccessToken(request)
                .or(() -> Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                        .filter(h -> h.startsWith("Bearer "))
                        .map(h -> h.substring(7).trim()))
                .filter(StringUtils::hasText);
    }
}