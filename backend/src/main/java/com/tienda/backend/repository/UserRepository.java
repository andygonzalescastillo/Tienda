package com.tienda.backend.repository;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndEstadoTrue(String email);

    long countByRolAndEstadoTrue(UserRole rol);

    List<User> findAllByRolAndEstadoTrue(UserRole rol);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.ultimaSesion = :ahora WHERE u.id = :id")
    void updateUltimaSesion(@Param("id") Long id, @Param("ahora") Instant ahora);

    @Query(value = """
            SELECT u FROM User u LEFT JOIN FETCH u.providers
            WHERE (:search IS NULL OR LOWER(u.email) LIKE :search
                   OR LOWER(u.nombre) LIKE :search
                   OR LOWER(u.apellido) LIKE :search)
              AND (:rol IS NULL OR u.rol = :rol)
              AND (:estado IS NULL OR u.estado = :estado)
            """,
            countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE (:search IS NULL OR LOWER(u.email) LIKE :search
                   OR LOWER(u.nombre) LIKE :search
                   OR LOWER(u.apellido) LIKE :search)
              AND (:rol IS NULL OR u.rol = :rol)
              AND (:estado IS NULL OR u.estado = :estado)
            """)
    Page<User> buscarUsuarios(
            @Param("search") String search,
            @Param("rol") UserRole rol,
            @Param("estado") Boolean estado,
            Pageable pageable
    );
}