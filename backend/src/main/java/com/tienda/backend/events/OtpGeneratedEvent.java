package com.tienda.backend.events;

import com.tienda.backend.domain.enums.VerificationCodeType;

public record OtpGeneratedEvent(
        String email,
        String codigo,
        VerificationCodeType type,
        long expirationMinutes
) {}