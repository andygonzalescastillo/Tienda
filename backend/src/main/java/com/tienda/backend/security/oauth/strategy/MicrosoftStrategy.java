package com.tienda.backend.security.oauth.strategy;

import com.tienda.backend.domain.enums.AuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class MicrosoftStrategy extends BaseOAuth2UserInfoStrategy {

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.MICROSOFT;
    }

    @Override
    public String getEmail(OAuth2User user) {
        return or(user, "email", "mail", "userPrincipalName");
    }

    @Override
    public String getFirstName(OAuth2User user) {
        String firstName = or(user, "givenName", "given_name");
        return !firstName.isBlank() ? firstName : or(user, "name", "displayName");
    }

    @Override
    public String getLastName(OAuth2User user) {
        return or(user, "surname", "family_name");
    }

    @Override
    public String getProviderId(OAuth2User user) {
        return or(user, "sub", "id");
    }
}