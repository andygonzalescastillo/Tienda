package com.tienda.backend.dto.auth.response;

import java.time.Instant;

public record SessionResponse(
        Long id,
        String tokenId,
        Long usuarioId,
        Instant fechaCreacion,
        Instant fechaExpiracion,
        String ipAddress,
        String userAgent,
        String ubicacion,
        Boolean revocado,
        Boolean esActual,
        String nombreDispositivo,
        String tipoDispositivo,
        Instant ultimoAcceso
) {}