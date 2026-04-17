package com.tienda.backend.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
        @NotBlank(message = "El secret JWT es obligatorio") String secret,
        @NotNull Token accessToken,
        @NotNull Token refreshToken,
        @Min(value = 1, message = "Debe permitirse al menos 1 sesión por usuario") int maxSessionsPerUser
) {
    public record Token(
            @Min(value = 1000, message = "La expiración debe ser al menos 1 segundo") long expiration
    ) {}
}