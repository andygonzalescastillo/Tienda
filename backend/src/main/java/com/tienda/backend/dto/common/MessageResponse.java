package com.tienda.backend.dto.common;

import java.util.Map;

public record MessageResponse(
        String successCode,
        Map<String, Object> metadata
) {
    public MessageResponse {
        if (metadata == null) {
            metadata = Map.of();
        }
    }

    public MessageResponse(String successCode) {
        this(successCode, Map.of());
    }
}