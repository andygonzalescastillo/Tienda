package com.tienda.backend.dto.websocket;

import java.time.Instant;

public record WebSocketMessageDto<T>(
        String type,
        T payload,
        Instant timestamp
) {
    public static WebSocketMessageDto<String> of(String type, String payload) {
        return new WebSocketMessageDto<>(type, payload, Instant.now());
    }

    public static WebSocketMessageDto<Void> of(String type) {
        return new WebSocketMessageDto<>(type, null, Instant.now());
    }
}
