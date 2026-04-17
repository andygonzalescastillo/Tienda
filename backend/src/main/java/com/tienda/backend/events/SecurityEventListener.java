package com.tienda.backend.events;

import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.websocket.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SecurityEventListener {

    private final TokenService tokenService;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordChanged(UserPasswordChangedEvent event) {
        tokenService.revocarTodosLosTokensDelUsuario(event.usuario(), "CAMBIO_PASSWORD");

        notificationService.enviarAUsuario(
                event.usuario().getEmail(),
                NotificationService.SESSION_EVENTS_QUEUE,
                WebSocketMessageDto.of("FORCE_LOGOUT")
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionCreated(SessionCreatedEvent event) {
        notificationService.enviarAUsuario(
                event.email(),
                NotificationService.SESSION_EVENTS_QUEUE,
                WebSocketMessageDto.of("SESSIONS_UPDATED")
        );
    }
}