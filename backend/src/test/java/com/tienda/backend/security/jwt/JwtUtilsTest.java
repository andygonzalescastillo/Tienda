package com.tienda.backend.security.jwt;

import com.tienda.backend.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtils - Generación y validación de tokens JWT")
class JwtUtilsTest {

    private static final String SECRET = "test-secret-key-que-debe-tener-al-menos-64-caracteres-para-HS512-algoritmo-seguro-test!!";
    private static final long ACCESS_EXPIRATION = 900_000L;
    private static final long REFRESH_EXPIRATION = 604_800_000L;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        var accessToken = new JwtProperties.Token(ACCESS_EXPIRATION);
        var refreshToken = new JwtProperties.Token(REFRESH_EXPIRATION);
        var properties = new JwtProperties(SECRET, accessToken, refreshToken, 5);

        jwtUtils = new JwtUtils(properties);
        jwtUtils.init();
    }

    @Nested
    @DisplayName("Generación de tokens")
    class GeneracionTokens {

        @Test
        @DisplayName("Debe generar un access token válido con los claims correctos")
        void debeGenerarAccessTokenValido() {
            String token = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "refresh-jti-123");

            assertThat(jwtUtils.esTokenValido(token)).isTrue();
            assertThat(jwtUtils.esAccessToken(token)).isTrue();
            assertThat(jwtUtils.esRefreshToken(token)).isFalse();

            Claims claims = jwtUtils.parsearClaims(token);
            assertThat(claims.getSubject()).isEqualTo("test@mail.com");
            assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
            assertThat(claims.get("rol", String.class)).isEqualTo("USER");
            assertThat(claims.get("refreshTokenJti", String.class)).isEqualTo("refresh-jti-123");
        }

        @Test
        @DisplayName("Debe generar un refresh token válido sin claim refreshTokenJti")
        void debeGenerarRefreshTokenValido() {
            String token = jwtUtils.generarRefreshToken("test@mail.com", 1L, "USER");

            assertThat(jwtUtils.esTokenValido(token)).isTrue();
            assertThat(jwtUtils.esRefreshToken(token)).isTrue();
            assertThat(jwtUtils.esAccessToken(token)).isFalse();

            Claims claims = jwtUtils.parsearClaims(token);
            assertThat(claims.getSubject()).isEqualTo("test@mail.com");
            assertThat(claims.get("refreshTokenJti", String.class)).isNull();
        }

        @Test
        @DisplayName("Cada token debe tener un JTI único")
        void cadaTokenDebeTenerJtiUnico() {
            String token1 = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "jti");
            String token2 = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "jti");

            assertThat(jwtUtils.obtenerJti(token1)).isNotEqualTo(jwtUtils.obtenerJti(token2));
        }

        @Test
        @DisplayName("El token debe tener fecha de expiración en el futuro")
        void tokenDebeTenerExpiracionFutura() {
            String token = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "jti");

            Date expiracion = jwtUtils.obtenerFechaExpiracion(token);
            assertThat(expiracion).isAfter(new Date());
        }
    }

    @Nested
    @DisplayName("Validación de tokens")
    class ValidacionTokens {

        @Test
        @DisplayName("Token válido debe retornar true")
        void tokenValidoDebeRetornarTrue() {
            String token = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "jti");

            assertThat(jwtUtils.esTokenValido(token)).isTrue();
        }

        @Test
        @DisplayName("Token con firma incorrecta debe retornar false")
        void tokenConFirmaIncorrectaDebeRetornarFalse() {
            String otraSecret = "otra-clave-secreta-diferente-que-tambien-debe-tener-al-menos-64-caracteres-para-HS512!!";
            SecretKey otraClave = Keys.hmacShaKeyFor(otraSecret.getBytes(StandardCharsets.UTF_8));

            String tokenFalso = Jwts.builder()
                    .subject("hacker@mail.com")
                    .signWith(otraClave, Jwts.SIG.HS512)
                    .expiration(new Date(System.currentTimeMillis() + 60000))
                    .compact();

            assertThat(jwtUtils.esTokenValido(tokenFalso)).isFalse();
        }

        @Test
        @DisplayName("Token expirado debe retornar false")
        void tokenExpiradoDebeRetornarFalse() {
            SecretKey clave = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

            String tokenExpirado = Jwts.builder()
                    .subject("test@mail.com")
                    .expiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(clave, Jwts.SIG.HS512)
                    .compact();

            assertThat(jwtUtils.esTokenValido(tokenExpirado)).isFalse();
        }

        @Test
        @DisplayName("String basura debe retornar false")
        void stringBasuraDebeRetornarFalse() {
            assertThat(jwtUtils.esTokenValido("esto-no-es-un-token")).isFalse();
        }

        @Test
        @DisplayName("Null debe retornar false")
        void nullDebeRetornarFalse() {
            assertThat(jwtUtils.esTokenValido(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Identificación de tipo de token")
    class TipoDeToken {

        @Test
        @DisplayName("esAccessToken debe ser true para access token")
        void esAccessTokenDebeSerTrueParaAccess() {
            String token = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "jti");

            assertThat(jwtUtils.esAccessToken(token)).isTrue();
            assertThat(jwtUtils.esRefreshToken(token)).isFalse();
        }

        @Test
        @DisplayName("esRefreshToken debe ser true para refresh token")
        void esRefreshTokenDebeSerTrueParaRefresh() {
            String token = jwtUtils.generarRefreshToken("test@mail.com", 1L, "USER");

            assertThat(jwtUtils.esRefreshToken(token)).isTrue();
            assertThat(jwtUtils.esAccessToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("Extracción de claims")
    class ExtraccionClaims {

        @Test
        @DisplayName("Debe obtener el JTI del token")
        void debeObtenerJti() {
            String token = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "jti");

            String jti = jwtUtils.obtenerJti(token);
            assertThat(jti).isNotBlank();
        }

        @Test
        @DisplayName("Debe obtener el refreshTokenJti desde un access token")
        void debeObtenerRefreshTokenJti() {
            String refreshJti = "mi-refresh-jti-unico";
            String accessToken = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", refreshJti);

            String resultado = jwtUtils.obtenerRefreshTokenJti(accessToken);
            assertThat(resultado).isEqualTo(refreshJti);
        }

        @Test
        @DisplayName("Debe obtener la fecha de expiración del token")
        void debeObtenerFechaExpiracion() {
            String token = jwtUtils.generarAccessToken("test@mail.com", 1L, "USER", "jti");

            Date expiracion = jwtUtils.obtenerFechaExpiracion(token);
            assertThat(expiracion).isNotNull();
            assertThat(expiracion.getTime()).isGreaterThan(System.currentTimeMillis());
        }
    }
}
