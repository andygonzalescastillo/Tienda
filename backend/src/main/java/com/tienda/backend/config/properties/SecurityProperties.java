package com.tienda.backend.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "security")
@Validated
public record SecurityProperties(
        @NotNull Otp otp
) {
    public record Otp(
            @Min(value = 1, message = "La expiración de OTP debe ser mayor a cero") long expirationMinutes,
            @Min(value = 1, message = "Los intentos de OTP deben ser mayor a cero") int maxAttempts
    ) {}
}