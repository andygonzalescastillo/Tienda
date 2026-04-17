package com.tienda.backend.service.websocket;

import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public static final String SESSION_EVENTS_QUEUE = "/queue/session-events";

    public void enviarAUsuario(String email, String destination, WebSocketMessageDto<?> message) {
        try {
            messagingTemplate.convertAndSendToUser(email, destination, message);
        } catch (Exception e) {
            log.warn("Error enviando WS a {} → {}: {}", email, destination, e.getMessage());
        }
    }

    public void enviarATodosLosAdmins(WebSocketMessageDto<?> message) {
        userRepository.findAllByRolAndEstadoTrue(UserRole.ADMIN).forEach(admin -> 
            enviarAUsuario(admin.getEmail(), SESSION_EVENTS_QUEUE, message)
        );
    }
}
