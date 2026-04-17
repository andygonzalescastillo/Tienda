package com.tienda.backend.security.jwt;

import com.tienda.backend.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;
    private SecretKey claveFirma;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.claveFirma = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
        this.jwtParser = Jwts.parser()
                .verifyWith(claveFirma)
                .build();
    }
    public Claims parsearClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public boolean esTokenValido(String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException _) {
            return false;
        }
    }

    public String generarAccessToken(String email, Long userId, String rol, String refreshTokenId) {
        return generarToken(email, userId, rol, "access", jwtProperties.accessToken().expiration(), refreshTokenId);
    }

    public String generarRefreshToken(String email, Long userId, String rol) {
        return generarToken(email, userId, rol, "refresh", jwtProperties.refreshToken().expiration(), null);
    }

    private String generarToken(String email, Long userId, String rol, String tipo, long expiracion, String refreshTokenId) {
        var builder = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("rol", rol)
                .claim("type", tipo)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracion))
                .signWith(claveFirma, Jwts.SIG.HS512);

        if (refreshTokenId != null) {
            builder.claim("refreshTokenJti", refreshTokenId);
        }
        return builder.compact();
    }

    public String obtenerJti(String token) {
        return parsearClaims(token).getId();
    }

    public boolean esRefreshToken(String token) {
        return "refresh".equals(parsearClaims(token).get("type", String.class));
    }

    public boolean esAccessToken(String token) {
        return "access".equals(parsearClaims(token).get("type", String.class));
    }

    public Date obtenerFechaExpiracion(String token) {
        return parsearClaims(token).getExpiration();
    }

    public String obtenerRefreshTokenJti(String token) {
        return parsearClaims(token).get("refreshTokenJti", String.class);
    }
}