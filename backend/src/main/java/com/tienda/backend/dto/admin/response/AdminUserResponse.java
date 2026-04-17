package com.tienda.backend.dto.admin.response;

import com.tienda.backend.domain.enums.UserRole;

import java.time.Instant;
import java.util.Set;

public record AdminUserResponse(
        Long id,
        String email,
        String nombre,
        String apellido,
        UserRole rol,
        Boolean estado,
        Boolean emailVerificado,
        Set<String> providers,
        Instant ultimaSesion,
        Instant fechaRegistro
) {
}
