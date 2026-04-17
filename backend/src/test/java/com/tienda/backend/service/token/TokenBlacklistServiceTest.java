package com.tienda.backend.service.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService")
class TokenBlacklistServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService service;

    @Nested
    @DisplayName("agregarAListaNegra")
    class AgregarAListaNegra {

        @Test
        @DisplayName("Debe agregar JTI a Redis con TTL correcto")
        void debeAgregarJtiConTTL() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            service.agregarAListaNegra("jti-123", 3600);

            verify(valueOperations).set("blacklist:jti-123", "revoked", Duration.ofSeconds(3600));
        }

        @Test
        @DisplayName("No debe agregar si TTL es 0 o negativo")
        void noDebeAgregarSiTTLEsCeroONegativo() {
            service.agregarAListaNegra("jti-123", 0);
            service.agregarAListaNegra("jti-456", -100);

            verifyNoInteractions(redisTemplate);
        }
    }

    @Nested
    @DisplayName("estaEnListaNegra")
    class EstaEnListaNegra {

        @Test
        @DisplayName("Debe retornar true si la clave existe en Redis")
        void debeRetornarTrueSiExiste() {
            when(redisTemplate.hasKey("blacklist:jti-revoked")).thenReturn(true);

            assertThat(service.estaEnListaNegra("jti-revoked")).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si la clave no existe en Redis")
        void debeRetornarFalseSiNoExiste() {
            when(redisTemplate.hasKey("blacklist:jti-valid")).thenReturn(false);

            assertThat(service.estaEnListaNegra("jti-valid")).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false si Redis retorna null")
        void debeRetornarFalseSiRedisRetornaNull() {
            when(redisTemplate.hasKey("blacklist:jti-null")).thenReturn(null);

            assertThat(service.estaEnListaNegra("jti-null")).isFalse();
        }
    }
}
