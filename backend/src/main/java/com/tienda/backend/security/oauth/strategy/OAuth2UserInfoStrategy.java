package com.tienda.backend.security.oauth.strategy;

import com.tienda.backend.domain.enums.AuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoStrategy {
    AuthProvider getProvider();
    String getEmail(OAuth2User user);
    String getFirstName(OAuth2User user);
    String getLastName(OAuth2User user);
    String getProviderId(OAuth2User user);
}