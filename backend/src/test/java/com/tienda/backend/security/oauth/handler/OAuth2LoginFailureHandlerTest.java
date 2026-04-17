package com.tienda.backend.security.oauth.handler;

import com.tienda.backend.config.properties.FrontendProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2LoginFailureHandler")
class OAuth2LoginFailureHandlerTest {

    @Mock private RedirectStrategy redirectStrategy;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private OAuth2LoginFailureHandler handler;

    private static final FrontendProperties FRONT_PROPS = new FrontendProperties(
            "http://localhost:3000", null, "/auth/callback", "/auth/error"
    );

    @BeforeEach
    void setUp() {
        handler = new OAuth2LoginFailureHandler(FRONT_PROPS);
        handler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    @DisplayName("Debe redirigir a frontend con parámetro de error")
    void debeRedirigirConParametroError() throws Exception {
        AuthenticationException exception = new BadCredentialsException("OAuth2 failed");

        handler.onAuthenticationFailure(request, response, exception);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), urlCaptor.capture());

        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).startsWith("http://localhost:3000/auth/callback");
        assertThat(redirectUrl).contains("error=");
    }

    @Test
    @DisplayName("Debe incluir URL del frontend correcta en la redirección")
    void debeIncluirUrlFrontendCorrecta() throws Exception {
        AuthenticationException exception = new BadCredentialsException("Provider error");

        handler.onAuthenticationFailure(request, response, exception);

        var urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), urlCaptor.capture());

        assertThat(urlCaptor.getValue()).contains("localhost:3000");
        assertThat(urlCaptor.getValue()).contains("/auth/callback");
    }
}
