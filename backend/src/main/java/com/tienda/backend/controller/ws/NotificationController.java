package com.tienda.backend.controller.ws;

import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.service.websocket.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @MessageMapping("/ping")
    public void handlePing(Principal principal) {
        if (principal != null) {
            notificationService.enviarAUsuario(
                    principal.getName(),
                    "/queue/pong",
                    WebSocketMessageDto.of("PONG", "Conexión WebSocket activa")
            );
        }
    }
}
