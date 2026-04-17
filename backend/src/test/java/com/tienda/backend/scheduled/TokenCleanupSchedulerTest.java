package com.tienda.backend.scheduled;

import com.tienda.backend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenCleanupScheduler - Limpieza programada")
class TokenCleanupSchedulerTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenCleanupScheduler scheduler;

    @Test
    @DisplayName("Debe ejecutar la limpieza de tokens en la base de datos")
    void limpiarTokensExpiradosYRevocados() {
        when(refreshTokenRepository.limpiarTokensExpiradosYRevocados(any())).thenReturn(5);

        scheduler.limpiarTokensExpiradosYRevocados();

        verify(refreshTokenRepository).limpiarTokensExpiradosYRevocados(any());
    }
}
