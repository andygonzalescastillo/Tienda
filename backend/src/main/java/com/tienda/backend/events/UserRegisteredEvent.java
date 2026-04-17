package com.tienda.backend.events;

import com.tienda.backend.domain.entity.User;

public record UserRegisteredEvent(User usuario) {}