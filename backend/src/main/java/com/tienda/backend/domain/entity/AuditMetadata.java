package com.tienda.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditMetadata {

    @CreatedDate
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    Instant fechaRegistro;

    @CreatedBy
    @Column(name = "usuario_registro", length = 255, updatable = false)
    String usuarioRegistro;

    @LastModifiedDate
    @Column(name = "fecha_ultima_modificacion")
    Instant fechaUltimaModificacion;

    @LastModifiedBy
    @Column(name = "usuario_ultima_modificacion", length = 255)
    String usuarioUltimaModificacion;
}