package com.tienda.backend.events;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.websocket.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityEventListener - Manejo de eventos de seguridad")
class SecurityEventListenerTest {

    @Mock private TokenService tokenService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private SecurityEventListener listener;

    @Test
    @DisplayName("Debe revocar tokens y enviar FORCE_LOGOUT al cambiar contraseña")
    void handlePasswordChanged() {
        User user = User.builder().email("test@mail.com").build();
        UserPasswordChangedEvent event = new UserPasswordChangedEvent(user);

        listener.handlePasswordChanged(event);

        verify(tokenService).revocarTodosLosTokensDelUsuario(user, "CAMBIO_PASSWORD");
        verify(notificationService).enviarAUsuario(
                eq("test@mail.com"),
                eq(NotificationService.SESSION_EVENTS_QUEUE),
                argThat(msg -> "FORCE_LOGOUT".equals(((WebSocketMessageDto<?>) msg).type()))
        );
    }

    @Test
    @DisplayName("Debe enviar SESSIONS_UPDATED al crear una nueva sesión")
    void handleSessionCreated() {
        SessionCreatedEvent event = new SessionCreatedEvent("test@mail.com");

        listener.handleSessionCreated(event);

        verify(notificationService).enviarAUsuario(
                eq("test@mail.com"),
                eq(NotificationService.SESSION_EVENTS_QUEUE),
                argThat(msg -> "SESSIONS_UPDATED".equals(((WebSocketMessageDto<?>) msg).type()))
        );
    }
}
