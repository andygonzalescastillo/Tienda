package com.tienda.backend.dto.auth.response;

public record SessionValidationResponse(
        String email,
        String nombre,
        String rol
) {}
