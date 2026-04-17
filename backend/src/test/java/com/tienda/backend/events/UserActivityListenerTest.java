package com.tienda.backend.events;

import com.tienda.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserActivityListener - Registro de actividad de usuarios")
class UserActivityListenerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserActivityListener listener;

    @Test
    @DisplayName("Debe actualizar última sesión en base de datos al autenticar")
    void handleAuthentication() {
        UserAuthenticatedEvent event = new UserAuthenticatedEvent(10L);

        listener.handleAuthentication(event);

        verify(userRepository).updateUltimaSesion(eq(10L), any());
    }
}
