package com.tienda.backend.repository;

import com.tienda.backend.domain.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenIdAndRevocadoFalse(String tokenId);
    List<RefreshToken> findByUserIdAndRevocadoFalseOrderByFechaCreacionDesc(Long userId);

    @Query("""
            SELECT COUNT(r) FROM RefreshToken r
            WHERE r.user.id = :usuarioId
              AND r.revocado = false
              AND r.fechaExpiracion > :ahora
            """)
    long contarSesionesActivas(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    @Modifying
    @Query("""
            UPDATE RefreshToken r
            SET r.revocado = true,
                r.fechaRevocacion = :fecha,
                r.razonRevocacion = :razon
            WHERE r.user.id = :usuarioId
              AND r.revocado = false
            """)
    void revocarTodosPorUsuario(@Param("usuarioId") Long usuarioId, @Param("fecha") Instant fecha, @Param("razon") String razon);

    @Query("""
            SELECT r FROM RefreshToken r
            WHERE r.user.id = :usuarioId
              AND r.revocado = false
              AND r.fechaExpiracion > :ahora
            ORDER BY r.fechaCreacion ASC
            """)
    List<RefreshToken> obtenerSesionesActivasOrdenadasPorAntiguedad(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    @Modifying
    @Transactional
    @Query("""
            UPDATE RefreshToken r
            SET r.ultimoAcceso = :ahora
            WHERE r.tokenId = :tokenId
              AND r.revocado = false
            """)
    int updateUltimoAcceso(@Param("tokenId") String tokenId, @Param("ahora") Instant ahora);

    @Modifying
    @Query("""
            DELETE FROM RefreshToken r
            WHERE r.revocado = true
               OR r.fechaExpiracion < :ahora
            """)
    int limpiarTokensExpiradosYRevocados(@Param("ahora") Instant ahora);
}
