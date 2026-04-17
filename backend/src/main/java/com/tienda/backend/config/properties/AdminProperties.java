package com.tienda.backend.config.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "admin.default")
@Validated
public record AdminProperties(
        @NotBlank(message = "El email del administrador es obligatorio en application.yml")
        @Email(message = "El formato del email del administrador es inválido")
        String email,

        @NotBlank(message = "La contraseña del administrador es obligatoria")
        @Size(min = 8, message = "La contraseña del administrador debe tener al menos 8 caracteres por seguridad")
        String password,

        String nombre,
        String apellido
) {}