package com.tienda.backend.service.token;

import com.tienda.backend.domain.entity.RefreshToken;
import com.tienda.backend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenValidationService")
class TokenValidationServiceTest {

    @Mock
    private TokenBlacklistService blacklistService;

    @Mock
    private RefreshTokenRepository refreshTokenRepo;

    @InjectMocks
    private TokenValidationService service;

    @Nested
    @DisplayName("estaRevocado (access token)")
    class EstaRevocado {

        @Test
        @DisplayName("Debe retornar true si el JTI está en lista negra")
        void debeRetornarTrueSiEstaEnListaNegra() {
            when(blacklistService.estaEnListaNegra("jti-revoked")).thenReturn(true);

            assertThat(service.estaRevocado("jti-revoked")).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si el JTI no está en lista negra")
        void debeRetornarFalseSiNoEstaEnListaNegra() {
            when(blacklistService.estaEnListaNegra("jti-valid")).thenReturn(false);

            assertThat(service.estaRevocado("jti-valid")).isFalse();
        }
    }

    @Nested
    @DisplayName("esRefreshTokenRevocado")
    class EsRefreshTokenRevocado {

        @Test
        @DisplayName("Debe retornar true si el JTI es null")
        void debeRetornarTrueSiJtiEsNull() {
            assertThat(service.esRefreshTokenRevocado(null)).isTrue();
        }

        @Test
        @DisplayName("Debe retornar true si el refresh token no existe en BD")
        void debeRetornarTrueSiNoExisteEnBD() {
            when(refreshTokenRepo.findByTokenIdAndRevocadoFalse("jti-unknown"))
                    .thenReturn(Optional.empty());

            assertThat(service.esRefreshTokenRevocado("jti-unknown")).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si el refresh token existe y no está revocado")
        void debeRetornarFalseSiExisteYNoRevocado() {
            var token = RefreshToken.builder()
                    .tokenId("jti-active")
                    .revocado(false)
                    .build();

            when(refreshTokenRepo.findByTokenIdAndRevocadoFalse("jti-active"))
                    .thenReturn(Optional.of(token));

            assertThat(service.esRefreshTokenRevocado("jti-active")).isFalse();
        }
    }
}
