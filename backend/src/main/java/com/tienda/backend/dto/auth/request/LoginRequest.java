package com.tienda.backend.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.tienda.backend.service.util.EmailNormalizer;

public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
    public LoginRequest {
        email = EmailNormalizer.normalize(email);
    }
}
