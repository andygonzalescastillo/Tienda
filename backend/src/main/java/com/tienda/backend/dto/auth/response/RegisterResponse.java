package com.tienda.backend.dto.auth.response;

import java.util.Map;

public record RegisterResponse(
        String email,
        String nombre,
        Map<String, Object> metadata
) {
    public RegisterResponse {
        if (metadata == null) {
            metadata = Map.of();
        }
    }

    public RegisterResponse(String email, String nombre) {
        this(email, nombre, Map.of());
    }
}