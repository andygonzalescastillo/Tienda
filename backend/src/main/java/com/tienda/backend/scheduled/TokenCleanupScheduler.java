package com.tienda.backend.scheduled;

import com.tienda.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepo;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void limpiarTokensExpiradosYRevocados() {
        int eliminados = refreshTokenRepo.limpiarTokensExpiradosYRevocados(Instant.now());
        if (eliminados > 0) {
            log.info("Limpieza programada: {} tokens expirados/revocados eliminados", eliminados);
        }
    }
}
