package com.tienda.backend.controller.ws;

import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.service.websocket.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController - Endpoints WebSocket")
class NotificationControllerTest {

    @Mock private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    @Test
    @DisplayName("Debe responder con PONG al enviar ping si el usuario está autenticado")
    void handlePing_Authenticated() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user@mail.com");

        controller.handlePing(principal);

        verify(notificationService).enviarAUsuario(
                eq("user@mail.com"),
                eq("/queue/pong"),
                argThat(msg -> "PONG".equals(((WebSocketMessageDto<?>) msg).type()))
        );
    }

    @Test
    @DisplayName("No debe hacer nada si el principal es nulo")
    void handlePing_Unauthenticated() {
        controller.handlePing(null);

        verifyNoInteractions(notificationService);
    }
}
