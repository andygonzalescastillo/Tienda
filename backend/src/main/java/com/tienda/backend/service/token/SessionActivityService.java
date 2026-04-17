package com.tienda.backend.service.token;

import com.tienda.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionActivityService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final Map<String, Instant> lastUpdatedCache = new ConcurrentHashMap<>();

    private static final long UPDATE_THRESHOLD_MINUTES = 5;

    @Async
    public void registrarActividad(String refreshTokenJti) {
        if (refreshTokenJti == null || refreshTokenJti.isBlank()) {
            return;
        }

        Instant ahora = Instant.now();
        Instant ultimaActualizacion = lastUpdatedCache.get(refreshTokenJti);

        if (ultimaActualizacion == null || ahora.minus(UPDATE_THRESHOLD_MINUTES, ChronoUnit.MINUTES).isAfter(ultimaActualizacion)) {
            
            try {
                int rowsAffected = refreshTokenRepository.updateUltimoAcceso(refreshTokenJti, ahora);
                
                if (rowsAffected > 0) {
                    lastUpdatedCache.put(refreshTokenJti, ahora);
                }
            } catch (Exception e) {
                log.warn("Error actualizando último acceso asíncrono para sesión {}: {}", refreshTokenJti, e.getMessage());
            }
        }
    }

    public void removerDeCache(String refreshTokenJti) {
        if (refreshTokenJti != null) {
            lastUpdatedCache.remove(refreshTokenJti);
        }
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void limpiarCacheObsoleto() {
        var umbral = Instant.now().minus(UPDATE_THRESHOLD_MINUTES * 2, ChronoUnit.MINUTES);
        int antes = lastUpdatedCache.size();
        lastUpdatedCache.entrySet().removeIf(entry -> entry.getValue().isBefore(umbral));
        int eliminados = antes - lastUpdatedCache.size();
        if (eliminados > 0) {
            log.debug("Cache de actividad limpiado: {} entradas eliminadas, {} restantes", eliminados, lastUpdatedCache.size());
        }
    }
}
