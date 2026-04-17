package com.tienda.backend.service.websocket;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationService service;

    @Nested
    @DisplayName("enviarAUsuario")
    class EnviarAUsuario {

        @Test
        @DisplayName("Debe enviar mensaje WebSocket al usuario correcto")
        void debeEnviarMensajeAlUsuario() {
            var message = WebSocketMessageDto.of("SESSION_REVOKED", "jti-123");

            service.enviarAUsuario("user@mail.com", "/queue/session-events", message);

            verify(messagingTemplate).convertAndSendToUser("user@mail.com", "/queue/session-events", message);
        }

        @Test
        @DisplayName("Debe usar la constante SESSION_EVENTS_QUEUE correctamente")
        void debeUsarConstanteSessionEventsQueue() {
            var message = WebSocketMessageDto.of("LOGOUT_ALL");

            service.enviarAUsuario("admin@mail.com", NotificationService.SESSION_EVENTS_QUEUE, message);

            verify(messagingTemplate).convertAndSendToUser("admin@mail.com", "/queue/session-events", message);
        }

        @Test
        @DisplayName("Debe capturar excepción sin propagar cuando falla el envío")
        void debeCapturaExcepcionSinPropagar() {
            var message = WebSocketMessageDto.of("TEST_EVENT", "payload");
            doThrow(new RuntimeException("WebSocket desconectado"))
                    .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

            assertThatCode(() -> service.enviarAUsuario("user@mail.com", "/queue/test", message))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe enviar mensaje con payload string correctamente")
        void debeEnviarMensajeConPayloadString() {
            var message = WebSocketMessageDto.of("NEW_SESSION", "session-token-id");

            service.enviarAUsuario("test@mail.com", "/queue/notifications", message);

            verify(messagingTemplate).convertAndSendToUser(
                    eq("test@mail.com"),
                    eq("/queue/notifications"),
                    argThat(msg -> {
                        @SuppressWarnings("unchecked")
                        var wsMsg = (WebSocketMessageDto<String>) msg;
                        return "NEW_SESSION".equals(wsMsg.type()) && "session-token-id".equals(wsMsg.payload());
                    })
            );
        }

        @Test
        @DisplayName("Debe enviar mensaje sin payload (Void) correctamente")
        void debeEnviarMensajeSinPayload() {
            var message = WebSocketMessageDto.of("FORCE_LOGOUT");

            service.enviarAUsuario("user@mail.com", "/queue/session-events", message);

            verify(messagingTemplate).convertAndSendToUser(
                    eq("user@mail.com"),
                    eq("/queue/session-events"),
                    argThat(msg -> {
                        @SuppressWarnings("unchecked")
                        var wsMsg = (WebSocketMessageDto<Void>) msg;
                        return "FORCE_LOGOUT".equals(wsMsg.type()) && wsMsg.payload() == null;
                    })
            );
        }
    }

    @Nested
    @DisplayName("enviarATodosLosAdmins")
    class EnviarATodosLosAdmins {

        @Test
        @DisplayName("Debe enviar mensaje a todos los usuarios ADMIN activos")
        void debeEnviarATodosLosAdmins() {
            var admin1 = User.builder().id(1L).email("admin1@mail.com").rol(UserRole.ADMIN).estado(true).build();
            var admin2 = User.builder().id(2L).email("admin2@mail.com").rol(UserRole.ADMIN).estado(true).build();

            when(userRepository.findAllByRolAndEstadoTrue(UserRole.ADMIN)).thenReturn(List.of(admin1, admin2));

            var message = WebSocketMessageDto.of("USERS_UPDATED");

            service.enviarATodosLosAdmins(message);

            verify(messagingTemplate).convertAndSendToUser("admin1@mail.com", NotificationService.SESSION_EVENTS_QUEUE, message);
            verify(messagingTemplate).convertAndSendToUser("admin2@mail.com", NotificationService.SESSION_EVENTS_QUEUE, message);
        }

        @Test
        @DisplayName("No debe fallar si no hay administradores")
        void noDebeFallarSiNoHayAdmins() {
            when(userRepository.findAllByRolAndEstadoTrue(UserRole.ADMIN)).thenReturn(List.of());

            var message = WebSocketMessageDto.of("USERS_UPDATED");

            assertThatCode(() -> service.enviarATodosLosAdmins(message))
                    .doesNotThrowAnyException();

            verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
        }

        @Test
        @DisplayName("Debe continuar enviando a otros admins si uno falla")
        void debeContinuarSiUnoFalla() {
            var admin1 = User.builder().id(1L).email("admin1@mail.com").rol(UserRole.ADMIN).estado(true).build();
            var admin2 = User.builder().id(2L).email("admin2@mail.com").rol(UserRole.ADMIN).estado(true).build();

            when(userRepository.findAllByRolAndEstadoTrue(UserRole.ADMIN)).thenReturn(List.of(admin1, admin2));
            doThrow(new RuntimeException("WS error"))
                    .when(messagingTemplate).convertAndSendToUser(eq("admin1@mail.com"), anyString(), any());

            var message = WebSocketMessageDto.of("USERS_UPDATED");

            assertThatCode(() -> service.enviarATodosLosAdmins(message))
                    .doesNotThrowAnyException();

            // Debe intentar enviar a ambos admins
            verify(messagingTemplate).convertAndSendToUser(eq("admin1@mail.com"), anyString(), any());
            verify(messagingTemplate).convertAndSendToUser(eq("admin2@mail.com"), anyString(), any());
        }
    }
}
