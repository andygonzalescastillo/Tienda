package com.tienda.backend.security.oauth.strategy;

import org.springframework.security.oauth2.core.user.OAuth2User;

public abstract class BaseOAuth2UserInfoStrategy implements OAuth2UserInfoStrategy {

    protected String val(OAuth2User user, String key) {
        Object value = user.getAttribute(key);
        return value != null ? String.valueOf(value).trim() : "";
    }

    protected String or(OAuth2User user, String... keys) {
        for (String key : keys) {
            String value = val(user, key);
            if (!value.isBlank()) return value;
        }
        return "";
    }
}