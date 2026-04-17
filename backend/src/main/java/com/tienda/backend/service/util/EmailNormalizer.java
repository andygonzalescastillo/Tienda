package com.tienda.backend.service.util;

public final class EmailNormalizer {
    
    private EmailNormalizer() {}
    
    public static String normalize(String email) {
        return email != null ? email.strip().toLowerCase() : "";
    }
}
