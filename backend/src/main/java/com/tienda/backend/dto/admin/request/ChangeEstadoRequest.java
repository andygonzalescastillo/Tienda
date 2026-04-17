package com.tienda.backend.dto.admin.request;

import jakarta.validation.constraints.NotNull;

public record ChangeEstadoRequest(
        @NotNull(message = "El estado es requerido")
        Boolean estado
) {
}
