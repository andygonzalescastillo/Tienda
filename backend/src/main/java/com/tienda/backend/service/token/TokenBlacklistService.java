package com.tienda.backend.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void agregarAListaNegra(String jti, long timeToLiveSeconds) {
        if (timeToLiveSeconds > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "revoked", Duration.ofSeconds(timeToLiveSeconds));
        }
    }

    public boolean estaEnListaNegra(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}