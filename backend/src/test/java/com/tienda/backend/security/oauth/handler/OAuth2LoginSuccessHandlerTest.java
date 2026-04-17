package com.tienda.backend.security.oauth.handler;

import com.tienda.backend.config.properties.FrontendProperties;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.security.oauth.OAuth2UserInfoFactory;
import com.tienda.backend.security.oauth.strategy.OAuth2UserInfoStrategy;
import com.tienda.backend.service.oauth2.OAuth2Service;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.util.ClientMetadataService;
import com.tienda.backend.service.util.ClientMetadataService.ClientInfo;
import com.tienda.backend.service.util.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2LoginSuccessHandler")
class OAuth2LoginSuccessHandlerTest {

    @Mock private OAuth2Service oauth2Service;
    @Mock private TokenService tokenService;
    @Mock private CookieService cookieService;
    @Mock private OAuth2UserInfoFactory oAuth2UserInfoFactory;
    @Mock private ClientMetadataService clientMetadataService;
    @Mock private RedirectStrategy redirectStrategy;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private OAuth2AuthenticationToken authToken;
    @Mock private OAuth2User oauth2User;
    @Mock private OAuth2UserInfoStrategy strategy;

    private OAuth2LoginSuccessHandler handler;

    private static final FrontendProperties FRONT_PROPS = new FrontendProperties(
            "http://localhost:3000", null, "/auth/callback", "/auth/error"
    );

    @BeforeEach
    void setUp() {
        handler = new OAuth2LoginSuccessHandler(
                FRONT_PROPS, oauth2Service, tokenService, cookieService, oAuth2UserInfoFactory, clientMetadataService
        );
        handler.setRedirectStrategy(redirectStrategy);
    }

    @Nested
    @DisplayName("onAuthenticationSuccess - Flujo exitoso")
    class FlujoExitoso {

        @Test
        @DisplayName("Debe procesar usuario OAuth2, generar tokens, setear cookies y redirigir")
        void debeProcesarUsuarioYRedirigir() throws Exception {
            var user = User.builder().id(1L).email("user@gmail.com").rol(UserRole.USER).build();
            var tokens = new LoginResult("access-jwt", "refresh-jwt", "jti-1", "jti-2", "user@gmail.com", "USER");
            var clientInfo = new ClientInfo("200.100.50.1", "Chrome/120");

            when(authToken.getPrincipal()).thenReturn(oauth2User);
            when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
            when(oAuth2UserInfoFactory.getStrategy("google")).thenReturn(strategy);
            when(strategy.getEmail(oauth2User)).thenReturn("user@gmail.com");
            when(strategy.getFirstName(oauth2User)).thenReturn("Juan");
            when(strategy.getLastName(oauth2User)).thenReturn("Pérez");
            when(strategy.getProviderId(oauth2User)).thenReturn("google-id-123");
            when(strategy.getProvider()).thenReturn(AuthProvider.GOOGLE);
            when(oauth2Service.procesarUsuarioOAuth2("user@gmail.com", "Juan", "Pérez", "GOOGLE", "google-id-123")).thenReturn(user);
            when(clientMetadataService.extraerClientInfo(request)).thenReturn(clientInfo);
            when(tokenService.generarParDeTokens(user, "200.100.50.1", "Chrome/120")).thenReturn(tokens);

            handler.onAuthenticationSuccess(request, response, authToken);

            verify(cookieService).agregarAccessTokenCookie(response, "access-jwt");
            verify(cookieService).agregarRefreshTokenCookie(response, "refresh-jwt");
            verify(redirectStrategy).sendRedirect(eq(request), eq(response),
                    eq("http://localhost:3000/auth/callback?auth=success"));
        }
    }

    @Nested
    @DisplayName("onAuthenticationSuccess - Errores")
    class Errores {

        @Test
        @DisplayName("Debe redirigir a error si email es null")
        void debeRedirigirAErrorSiEmailEsNull() throws Exception {
            when(authToken.getPrincipal()).thenReturn(oauth2User);
            when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
            when(oAuth2UserInfoFactory.getStrategy("google")).thenReturn(strategy);
            when(strategy.getEmail(oauth2User)).thenReturn(null);

            handler.onAuthenticationSuccess(request, response, authToken);

            verify(response).sendRedirect(contains("/auth/error"));
            verifyNoInteractions(tokenService);
        }

        @Test
        @DisplayName("Debe redirigir a error si email está vacío")
        void debeRedirigirAErrorSiEmailVacio() throws Exception {
            when(authToken.getPrincipal()).thenReturn(oauth2User);
            when(authToken.getAuthorizedClientRegistrationId()).thenReturn("facebook");
            when(oAuth2UserInfoFactory.getStrategy("facebook")).thenReturn(strategy);
            when(strategy.getEmail(oauth2User)).thenReturn("");

            handler.onAuthenticationSuccess(request, response, authToken);

            verify(response).sendRedirect(contains("/auth/error"));
            verifyNoInteractions(tokenService);
        }

        @Test
        @DisplayName("Debe redirigir a error si OAuth2Service lanza excepción")
        void debeRedirigirAErrorSiOAuth2ServiceFalla() throws Exception {
            when(authToken.getPrincipal()).thenReturn(oauth2User);
            when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
            when(oAuth2UserInfoFactory.getStrategy("google")).thenReturn(strategy);
            when(strategy.getEmail(oauth2User)).thenReturn("user@gmail.com");
            when(strategy.getFirstName(oauth2User)).thenReturn("Juan");
            when(strategy.getLastName(oauth2User)).thenReturn("Pérez");
            when(strategy.getProviderId(oauth2User)).thenReturn("id-123");
            when(strategy.getProvider()).thenReturn(AuthProvider.GOOGLE);
            when(oauth2Service.procesarUsuarioOAuth2(anyString(), anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(AppException.unauthorized("OAUTH2_AUTHENTICATION_FAILED"));

            handler.onAuthenticationSuccess(request, response, authToken);

            verify(response).sendRedirect(contains("/auth/error"));
            verifyNoInteractions(cookieService);
        }

        @Test
        @DisplayName("Debe redirigir a error si strategy no encontrada")
        void debeRedirigirAErrorSiStrategyNoEncontrada() throws Exception {
            when(authToken.getPrincipal()).thenReturn(oauth2User);
            when(authToken.getAuthorizedClientRegistrationId()).thenReturn("unknown");
            when(oAuth2UserInfoFactory.getStrategy("unknown"))
                    .thenThrow(AppException.unauthorized("OAUTH2_UNSUPPORTED_PROVIDER"));

            handler.onAuthenticationSuccess(request, response, authToken);

            verify(response).sendRedirect(contains("/auth/error"));
            verifyNoInteractions(oauth2Service, tokenService, cookieService);
        }
    }
}
