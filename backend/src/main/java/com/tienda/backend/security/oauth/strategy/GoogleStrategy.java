package com.tienda.backend.security.oauth.strategy;

import com.tienda.backend.domain.enums.AuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleStrategy extends BaseOAuth2UserInfoStrategy {

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public String getEmail(OAuth2User user) {
        return val(user, "email");
    }

    @Override
    public String getFirstName(OAuth2User user) {
        return val(user, "given_name");
    }

    @Override
    public String getLastName(OAuth2User user) {
        return val(user, "family_name");
    }

    @Override
    public String getProviderId(OAuth2User user) {
        return val(user, "sub");
    }
}