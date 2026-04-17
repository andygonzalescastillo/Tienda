package com.tienda.backend.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.tienda.backend.service.util.EmailNormalizer;

public record VerifyCodeRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es inválido")
        String email,

        @NotBlank(message = "El código es obligatorio")
        @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
        String codigo
) {
    public VerifyCodeRequest {
        email = EmailNormalizer.normalize(email);
    }
}
