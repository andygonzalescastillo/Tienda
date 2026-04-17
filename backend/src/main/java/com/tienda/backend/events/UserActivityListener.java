package com.tienda.backend.events;

import com.tienda.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserActivityListener {

    private final UserRepository repo;

    @Async
    @EventListener
    public void handleAuthentication(UserAuthenticatedEvent event) {
        repo.updateUltimaSesion(event.userId(), Instant.now());
    }
}