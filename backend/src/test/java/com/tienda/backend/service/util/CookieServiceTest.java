package com.tienda.backend.service.util;

import com.tienda.backend.config.properties.CookieProperties;
import com.tienda.backend.config.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CookieService - Gestión de cookies HTTP")
class CookieServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private CookieProperties cookieProps;

    @InjectMocks
    private CookieService cookieService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        lenient().when(cookieProps.secure()).thenReturn(false);
        lenient().when(cookieProps.domain()).thenReturn("localhost");
    }

    @Nested
    @DisplayName("Agregar cookies")
    class AgregarCookies {

        @Test
        @DisplayName("Debe agregar cookie de access token con maxAge correcto")
        void debeAgregarAccessTokenCookie() {
            when(jwtProperties.accessToken()).thenReturn(new JwtProperties.Token(900_000L));

            cookieService.agregarAccessTokenCookie(response, "my-access-token");

            ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
            verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

            String cookieStr = headerCaptor.getValue();
            assertThat(cookieStr).contains("access_token=my-access-token");
            assertThat(cookieStr).contains("Max-Age=900");
            assertThat(cookieStr).contains("HttpOnly");
            assertThat(cookieStr).contains("SameSite=Lax");
        }

        @Test
        @DisplayName("Debe agregar cookie de refresh token con maxAge correcto")
        void debeAgregarRefreshTokenCookie() {
            when(jwtProperties.refreshToken()).thenReturn(new JwtProperties.Token(604_800_000L));

            cookieService.agregarRefreshTokenCookie(response, "my-refresh-token");

            ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
            verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

            String cookieStr = headerCaptor.getValue();
            assertThat(cookieStr).contains("refresh_token=my-refresh-token");
            assertThat(cookieStr).contains("Max-Age=604800");
        }
    }

    @Nested
    @DisplayName("Eliminar cookies")
    class EliminarCookies {

        @Test
        @DisplayName("Debe eliminar ambas cookies con Max-Age=0")
        void debeEliminarCookies() {
            cookieService.eliminarCookies(response);

            ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
            verify(response, times(2)).addHeader(eq("Set-Cookie"), headerCaptor.capture());

            assertThat(headerCaptor.getAllValues()).anySatisfy(c ->
                    assertThat(c).contains("access_token=").contains("Max-Age=0")
            );
            assertThat(headerCaptor.getAllValues()).anySatisfy(c ->
                    assertThat(c).contains("refresh_token=").contains("Max-Age=0")
            );
        }
    }

    @Nested
    @DisplayName("Obtener tokens de cookies")
    class ObtenerTokens {

        @Test
        @DisplayName("Debe obtener access token de la cookie")
        void debeObtenerAccessToken() {
            Cookie[] cookies = { new Cookie("access_token", "my-access"), new Cookie("other", "val") };
            when(request.getCookies()).thenReturn(cookies);

            Optional<String> result = cookieService.obtenerAccessToken(request);

            assertThat(result).isPresent().contains("my-access");
        }

        @Test
        @DisplayName("Debe obtener refresh token de la cookie")
        void debeObtenerRefreshToken() {
            Cookie[] cookies = { new Cookie("refresh_token", "my-refresh") };
            when(request.getCookies()).thenReturn(cookies);

            Optional<String> result = cookieService.obtenerRefreshToken(request);

            assertThat(result).isPresent().contains("my-refresh");
        }

        @Test
        @DisplayName("Debe retornar vacío si no hay cookies")
        void debeRetornarVacioSinCookies() {
            when(request.getCookies()).thenReturn(null);

            Optional<String> result = cookieService.obtenerAccessToken(request);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar vacío si la cookie no existe")
        void debeRetornarVacioSiCookieNoExiste() {
            Cookie[] cookies = { new Cookie("otra_cookie", "valor") };
            when(request.getCookies()).thenReturn(cookies);

            Optional<String> result = cookieService.obtenerRefreshToken(request);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar vacío si la cookie tiene valor vacío")
        void debeRetornarVacioSiValorVacio() {
            Cookie[] cookies = { new Cookie("access_token", "") };
            when(request.getCookies()).thenReturn(cookies);

            Optional<String> result = cookieService.obtenerAccessToken(request);

            assertThat(result).isEmpty();
        }
    }
}
