package com.tienda.backend.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "spring.mail")
@Validated
public record MailCustomProperties(
        @NotBlank String username,
        @NotBlank String senderName
) {}