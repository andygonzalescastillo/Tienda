package com.tienda.backend.dto.auth.response;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String successCode,
        String email,
        String nombre,
        String rol
) {
    public LoginResponse toResponse() {
        return new LoginResponse(successCode, email, nombre, rol);
    }
}
