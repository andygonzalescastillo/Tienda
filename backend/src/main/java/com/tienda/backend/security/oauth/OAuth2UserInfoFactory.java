package com.tienda.backend.security.oauth;

import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.security.oauth.strategy.OAuth2UserInfoStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuth2UserInfoFactory {

    private final Map<AuthProvider, OAuth2UserInfoStrategy> strategies;

    public OAuth2UserInfoFactory(List<OAuth2UserInfoStrategy> strategyList) {
        this.strategies = strategyList.stream().collect(Collectors.toMap(OAuth2UserInfoStrategy::getProvider, Function.identity()));
    }

    public OAuth2UserInfoStrategy getStrategy(String registrationId) {
        AuthProvider provider = resolveProvider(registrationId);

        return Optional.ofNullable(strategies.get(provider))
                .orElseThrow(() -> AppException.internalError("OAUTH2_STRATEGY_NOT_FOUND", Map.of("provider", registrationId)));
    }

    private AuthProvider resolveProvider(String regId) {
        try {
            return AuthProvider.valueOf(regId.toUpperCase().trim());
        } catch (IllegalArgumentException _) {
            throw AppException.unauthorized("OAUTH2_UNSUPPORTED_PROVIDER");
        }
    }
}