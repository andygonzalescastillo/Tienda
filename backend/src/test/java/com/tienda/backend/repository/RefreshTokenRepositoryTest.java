package com.tienda.backend.repository;

import com.tienda.backend.config.security.AuditConfig;
import com.tienda.backend.domain.entity.RefreshToken;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@DisplayName("RefreshTokenRepository - Queries de tokens de refresco")
class RefreshTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User usuario;
    private RefreshToken tokenActivo;
    private RefreshToken tokenRevocado;
    private RefreshToken tokenExpirado;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .email("test@mail.com")
                .password("hashed")
                .nombre("Juan")
                .apellido("Pérez")
                .rol(UserRole.USER)
                .emailVerificado(true)
                .estado(true)
                .build();
        usuario.getAudit().setUsuarioRegistro("test");
        entityManager.persistAndFlush(usuario);

        tokenActivo = RefreshToken.builder()
                .tokenId("active-jti")
                .tokenHash("hash-1")
                .user(usuario)
                .fechaExpiracion(Instant.now().plus(7, ChronoUnit.DAYS))
                .fechaCreacion(Instant.now())
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla")
                .revocado(false)
                .build();
        entityManager.persistAndFlush(tokenActivo);

        tokenRevocado = RefreshToken.builder()
                .tokenId("revoked-jti")
                .tokenHash("hash-2")
                .user(usuario)
                .fechaExpiracion(Instant.now().plus(7, ChronoUnit.DAYS))
                .fechaCreacion(Instant.now().minus(1, ChronoUnit.DAYS))
                .ipAddress("127.0.0.1")
                .revocado(true)
                .fechaRevocacion(Instant.now())
                .razonRevocacion("LOGOUT")
                .build();
        entityManager.persistAndFlush(tokenRevocado);

        tokenExpirado = RefreshToken.builder()
                .tokenId("expired-jti")
                .tokenHash("hash-3")
                .user(usuario)
                .fechaExpiracion(Instant.now().minus(1, ChronoUnit.DAYS))
                .fechaCreacion(Instant.now().minus(8, ChronoUnit.DAYS))
                .ipAddress("127.0.0.1")
                .revocado(false)
                .build();
        entityManager.persistAndFlush(tokenExpirado);
    }

    @Nested
    @DisplayName("findByTokenIdAndRevocadoFalse")
    class FindByTokenIdAndRevocadoFalse {

        @Test
        @DisplayName("Debe encontrar token activo no revocado")
        void debeEncontrarTokenActivo() {
            Optional<RefreshToken> result = refreshTokenRepository.findByTokenIdAndRevocadoFalse("active-jti");

            assertThat(result).isPresent();
            assertThat(result.get().getTokenId()).isEqualTo("active-jti");
        }

        @Test
        @DisplayName("No debe encontrar token revocado")
        void noDebeEncontrarTokenRevocado() {
            Optional<RefreshToken> result = refreshTokenRepository.findByTokenIdAndRevocadoFalse("revoked-jti");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar vacío para tokenId inexistente")
        void debeRetornarVacioParaInexistente() {
            Optional<RefreshToken> result = refreshTokenRepository.findByTokenIdAndRevocadoFalse("no-existe");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("contarSesionesActivas")
    class ContarSesionesActivas {

        @Test
        @DisplayName("Debe contar solo tokens activos y no expirados")
        void debeContarSoloActivos() {
            long count = refreshTokenRepository.contarSesionesActivas(usuario.getId(), Instant.now());

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("revocarTodosPorUsuario")
    class RevocarTodosPorUsuario {

        @Test
        @DisplayName("Debe revocar todos los tokens no revocados del usuario")
        void debeRevocarTodos() {
            refreshTokenRepository.revocarTodosPorUsuario(usuario.getId(), Instant.now(), "TEST");
            entityManager.clear();

            Optional<RefreshToken> result = refreshTokenRepository.findByTokenIdAndRevocadoFalse("active-jti");
            assertThat(result).isEmpty();

            Optional<RefreshToken> expired = refreshTokenRepository.findByTokenIdAndRevocadoFalse("expired-jti");
            assertThat(expired).isEmpty();
        }
    }

    @Nested
    @DisplayName("obtenerSesionesActivasOrdenadasPorAntiguedad")
    class ObtenerSesionesActivas {

        @Test
        @DisplayName("Debe retornar solo sesiones activas y no expiradas")
        void debeRetornarSesionesActivas() {
            List<RefreshToken> result = refreshTokenRepository
                    .obtenerSesionesActivasOrdenadasPorAntiguedad(usuario.getId(), Instant.now());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTokenId()).isEqualTo("active-jti");
        }
    }

    @Nested
    @DisplayName("updateUltimoAcceso")
    class UpdateUltimoAcceso {

        @Test
        @DisplayName("Debe actualizar último acceso para token activo")
        void debeActualizarUltimoAcceso() {
            Instant nuevoAcceso = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(java.time.temporal.ChronoUnit.MICROS);

            int updated = refreshTokenRepository.updateUltimoAcceso("active-jti", nuevoAcceso);
            entityManager.clear();

            assertThat(updated).isEqualTo(1);
            RefreshToken token = refreshTokenRepository.findByTokenIdAndRevocadoFalse("active-jti").orElseThrow();
            assertThat(token.getUltimoAcceso()).isEqualTo(nuevoAcceso);
        }

        @Test
        @DisplayName("No debe actualizar último acceso para token revocado")
        void noDebeActualizarTokenRevocado() {
            int updated = refreshTokenRepository.updateUltimoAcceso("revoked-jti", Instant.now());

            assertThat(updated).isZero();
        }
    }

    @Nested
    @DisplayName("limpiarTokensExpiradosYRevocados")
    class LimpiarTokens {

        @Test
        @DisplayName("Debe eliminar tokens expirados y revocados")
        void debeEliminarTokensExpiradosYRevocados() {
            int deleted = refreshTokenRepository.limpiarTokensExpiradosYRevocados(Instant.now());
            entityManager.clear();

            assertThat(deleted).isEqualTo(2);

            long total = refreshTokenRepository.count();
            assertThat(total).isEqualTo(1);
        }
    }
}
