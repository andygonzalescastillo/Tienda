package com.tienda.backend.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "frontend")
@Validated
public record FrontendProperties(
        @NotBlank String url,
        String adminUrl,
        @NotBlank String oauthCallbackPath,
        @NotBlank String errorPath
) {}