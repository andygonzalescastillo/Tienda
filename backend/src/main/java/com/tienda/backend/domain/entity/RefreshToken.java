package com.tienda.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long id;

    @Column(name = "token_id", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    String tokenId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    User user;

    @Column(name = "token_hash", nullable = false)
    String tokenHash;

    @Column(name = "fecha_expiracion", nullable = false)
    Instant fechaExpiracion;

    @Column(name = "fecha_creacion", nullable = false)
    @Builder.Default
    Instant fechaCreacion = Instant.now();

    @Column(name = "ip_address", length = 45)
    String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    String userAgent;

    @Column(name = "ubicacion", length = 150)
    String ubicacion;

    @Column(name = "revocado", nullable = false)
    @Builder.Default
    Boolean revocado = false;

    @Column(name = "fecha_revocacion")
    Instant fechaRevocacion;

    @Column(name = "razon_revocacion", length = 100)
    String razonRevocacion;

    @Column(name = "ultimo_acceso")
    @Builder.Default
    Instant ultimoAcceso = Instant.now();
}