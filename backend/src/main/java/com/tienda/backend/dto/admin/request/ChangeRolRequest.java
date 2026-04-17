package com.tienda.backend.dto.admin.request;

import com.tienda.backend.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRolRequest(
        @NotNull(message = "El rol es requerido")
        UserRole rol
) {
}
