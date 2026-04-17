package com.tienda.backend.security.oauth.strategy;

import com.tienda.backend.domain.enums.AuthProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GithubStrategy extends BaseOAuth2UserInfoStrategy {

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GITHUB;
    }

    @Override
    public String getEmail(OAuth2User user) {
        String email = val(user, "email");
        if (email.isBlank()) {
            if (user.getAttribute("emails") instanceof List<?> emails && !emails.isEmpty()) {
                if (emails.getFirst() instanceof Map<?, ?> emailMap) {
                    return String.valueOf(emailMap.get("email"));
                }
            }
        }
        return email;
    }

    @Override
    public String getFirstName(OAuth2User user) {
        String name = val(user, "name");
        if (name.isBlank()) return val(user, "login");

        return name.contains(" ") ? name.split(" ")[0] : name;
    }

    @Override
    public String getLastName(OAuth2User user) {
        String name = val(user, "name");
        if (name.isBlank() || !name.contains(" ")) return "";
        return name.substring(name.indexOf(" ") + 1);
    }

    @Override
    public String getProviderId(OAuth2User user) {
        return val(user, "id");
    }
}