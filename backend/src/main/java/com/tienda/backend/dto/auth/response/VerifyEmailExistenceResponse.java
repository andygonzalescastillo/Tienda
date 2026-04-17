package com.tienda.backend.dto.auth.response;

import java.util.List;

public record VerifyEmailExistenceResponse(
        Boolean existe,
        Boolean tienePassword,
        String proveedor,
        List<String> proveedoresVinculados
) {}