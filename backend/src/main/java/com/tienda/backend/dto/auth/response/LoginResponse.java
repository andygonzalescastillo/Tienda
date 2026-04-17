package com.tienda.backend.dto.auth.response;

public record LoginResponse(
        String successCode,
        String email,
        String nombre,
        String rol
) {}
