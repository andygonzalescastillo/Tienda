package com.tienda.backend.security.oauth.handler;

import com.tienda.backend.config.properties.FrontendProperties;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.security.oauth.OAuth2UserInfoFactory;
import com.tienda.backend.security.oauth.strategy.OAuth2UserInfoStrategy;
import com.tienda.backend.service.oauth2.OAuth2Service;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.util.ClientMetadataService;
import com.tienda.backend.service.util.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final FrontendProperties frontendProps;
    private final OAuth2Service oauth2Service;
    private final TokenService tokenService;
    private final CookieService cookieService;
    private final OAuth2UserInfoFactory oAuth2UserInfoFactory;
    private final ClientMetadataService clientMetadataService;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull Authentication auth) throws IOException {
        try {
            var oauthToken = (OAuth2AuthenticationToken) auth;
            var principal = oauthToken.getPrincipal();
            var registrationId = oauthToken.getAuthorizedClientRegistrationId();

            OAuth2UserInfoStrategy strategy = oAuth2UserInfoFactory.getStrategy(registrationId);

            var email = strategy.getEmail(principal);
            var firstName = strategy.getFirstName(principal);
            var lastName = strategy.getLastName(principal);
            var providerId = strategy.getProviderId(principal);
            var providerName = strategy.getProvider().name();

            if (email == null || email.isBlank()) {
                throw AppException.unauthorized("OAUTH2_AUTHENTICATION_FAILED");
            }

            User user = oauth2Service.procesarUsuarioOAuth2(
                    email,
                    firstName,
                    lastName,
                    providerName,
                    providerId
            );
            var clientInfo = clientMetadataService.extraerClientInfo(req);
            var tokens = tokenService.generarParDeTokens(user, clientInfo.ip(), clientInfo.userAgent());
            cookieService.agregarAccessTokenCookie(res, tokens.accessToken());
            cookieService.agregarRefreshTokenCookie(res, tokens.refreshToken());

            getRedirectStrategy().sendRedirect(req, res, buildUrl(frontendProps.oauthCallbackPath(), "auth", "success"));

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Error desconocido";
            res.sendRedirect(buildUrl(frontendProps.errorPath(), null, URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)));
        }
    }

    private String buildUrl(String path, String paramKey, String paramValue) {
        var builder = UriComponentsBuilder.fromUriString(frontendProps.url() + path);
        if (paramKey != null && paramValue != null) {
            builder.queryParam(paramKey, paramValue);
        }
        return builder.build().toUriString();
    }
}