package com.tienda.backend.service.token;

import com.tienda.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final TokenBlacklistService blacklistService;
    private final RefreshTokenRepository refreshTokenRepo;

    public boolean estaRevocado(String jti) {
        return blacklistService.estaEnListaNegra(jti);
    }

    @Transactional(readOnly = true)
    public boolean esRefreshTokenRevocado(String refreshTokenJti) {
        if (refreshTokenJti == null) return true;
        return !refreshTokenRepo.findByTokenIdAndRevocadoFalse(refreshTokenJti).isPresent();
    }
}
