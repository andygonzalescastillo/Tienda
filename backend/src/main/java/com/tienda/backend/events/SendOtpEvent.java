package com.tienda.backend.events;

import com.tienda.backend.domain.enums.VerificationCodeType;

public record SendOtpEvent(
        String email,
        VerificationCodeType type
) {}