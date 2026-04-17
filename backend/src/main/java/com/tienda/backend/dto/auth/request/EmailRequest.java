package com.tienda.backend.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.tienda.backend.service.util.EmailNormalizer;

public record EmailRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es inválido")
        String email
) {
    public EmailRequest {
        email = EmailNormalizer.normalize(email);
    }
}
