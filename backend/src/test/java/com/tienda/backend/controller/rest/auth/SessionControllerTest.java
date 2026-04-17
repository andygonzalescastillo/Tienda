package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.SessionResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.exception.ManejadorExcepcionesREST;
import com.tienda.backend.security.jwt.JwtAuthenticationFilter;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.util.ClientMetadataService;
import com.tienda.backend.service.util.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SessionController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@ActiveProfiles("test")
@Import(ManejadorExcepcionesREST.class)
@DisplayName("SessionController - Endpoints de sesión")
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private ClientMetadataService clientMetadataService;

    @Nested
    @DisplayName("POST /auth/refresh")
    class PostRefresh {

        @Test
        @DisplayName("Debe refrescar token y retornar 200 con nueva respuesta")
        void debeRefrescarTokenExitosamente() throws Exception {
            var clientInfo = new ClientMetadataService.ClientInfo("127.0.0.1", "Mozilla");
            var loginResult = new LoginResult("new-access", "new-refresh", "LOGIN_SUCCESS", "test@mail.com", "Juan", "USER");

            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of("old-refresh"));
            when(clientMetadataService.extraerClientInfo(any(HttpServletRequest.class))).thenReturn(clientInfo);
            when(tokenService.refrescarToken(anyString(), anyString(), anyString())).thenReturn(loginResult);

            mockMvc.perform(post("/auth/refresh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("LOGIN_SUCCESS"))
                    .andExpect(jsonPath("$.email").value("test@mail.com"))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.rol").value("USER"));

            verify(cookieService).agregarAccessTokenCookie(any(), anyString());
            verify(cookieService).agregarRefreshTokenCookie(any(), anyString());
        }

        @Test
        @DisplayName("Debe retornar 401 si no hay refresh token en cookie")
        void debeRetornar401SinRefreshToken() throws Exception {
            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

            mockMvc.perform(post("/auth/refresh"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("REFRESH_TOKEN_NOT_FOUND"));
        }

        @Test
        @DisplayName("Debe retornar 401 si el refresh token es inválido")
        void debeRetornar401RefreshTokenInvalido() throws Exception {
            var clientInfo = new ClientMetadataService.ClientInfo("127.0.0.1", "Mozilla");

            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of("invalid-token"));
            when(clientMetadataService.extraerClientInfo(any(HttpServletRequest.class))).thenReturn(clientInfo);
            when(tokenService.refrescarToken(anyString(), anyString(), anyString()))
                    .thenThrow(AppException.unauthorized("INVALID_REFRESH_TOKEN"));

            mockMvc.perform(post("/auth/refresh"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"));
        }
    }

    @Nested
    @DisplayName("POST /auth/logout")
    class PostLogout {

        @Test
        @DisplayName("Debe cerrar sesión y retornar 200")
        void debeCerrarSesionExitosamente() throws Exception {
            when(cookieService.obtenerAccessToken(any(HttpServletRequest.class))).thenReturn(Optional.of("access-token"));
            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of("refresh-token"));

            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("LOGOUT_SUCCESS"));

            verify(tokenService).cerrarSesion(anyString(), anyString());
            verify(cookieService).eliminarCookies(any());
        }

        @Test
        @DisplayName("Debe cerrar sesión incluso sin cookies (graceful)")
        void debeCerrarSesionSinCookies() throws Exception {
            when(cookieService.obtenerAccessToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());
            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("LOGOUT_SUCCESS"));

            verify(cookieService).eliminarCookies(any());
        }
    }

    @Nested
    @DisplayName("POST /auth/logout-all")
    class PostLogoutAll {

        @Test
        @DisplayName("Debe cerrar todas las sesiones y retornar 200")
        void debeCerrarTodasLasSesionesExitosamente() throws Exception {
            when(cookieService.obtenerAccessToken(any(HttpServletRequest.class))).thenReturn(Optional.of("access-token"));

            mockMvc.perform(post("/auth/logout-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("LOGOUT_ALL_SUCCESS"));

            verify(tokenService).revocarAccessToken(anyString(), anyString());
            verify(tokenService).cerrarTodasLasSesionesDelUsuario(any());
            verify(cookieService).eliminarCookies(any());
        }

        @Test
        @DisplayName("Debe cerrar todas las sesiones incluso sin access token cookie")
        void debeCerrarSesionesSinAccessToken() throws Exception {
            when(cookieService.obtenerAccessToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

            mockMvc.perform(post("/auth/logout-all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("LOGOUT_ALL_SUCCESS"));

            verify(tokenService).cerrarTodasLasSesionesDelUsuario(any());
            verify(cookieService).eliminarCookies(any());
        }
    }

    @Nested
    @DisplayName("GET /auth/sessions")
    class GetSessions {

        @Test
        @DisplayName("Debe retornar lista de sesiones activas")
        void debeRetornarSesionesActivas() throws Exception {
            var now = Instant.now();
            var session1 = new SessionResponse(1L, "jti-1", 1L, now, now.plus(24, ChronoUnit.HOURS), "192.168.1.1", "Chrome", "Lima", false, true, "Chrome Desktop", "DESKTOP", now);
            var session2 = new SessionResponse(2L, "jti-2", 1L, now.minus(1, ChronoUnit.DAYS), now.plus(12, ChronoUnit.HOURS), "10.0.0.1", "Firefox", "Arequipa", false, false, "Firefox Mobile", "MOBILE", now.minus(2, ChronoUnit.HOURS));

            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of("refresh-token"));
            when(tokenService.obtenerJtiRefreshToken(anyString())).thenReturn("jti-1");
            when(tokenService.obtenerSesionesActivas(any(), anyString())).thenReturn(List.of(session1, session2));

            mockMvc.perform(get("/auth/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].tokenId").value("jti-1"))
                    .andExpect(jsonPath("$[0].esActual").value(true))
                    .andExpect(jsonPath("$[1].tokenId").value("jti-2"))
                    .andExpect(jsonPath("$[1].esActual").value(false));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay sesiones")
        void debeRetornarListaVacia() throws Exception {
            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());
            when(tokenService.obtenerJtiRefreshToken(any())).thenReturn(null);
            when(tokenService.obtenerSesionesActivas(any(), any())).thenReturn(List.of());

            mockMvc.perform(get("/auth/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("DELETE /auth/sessions/{tokenId}")
    class DeleteSessionEspecifica {

        @Test
        @DisplayName("Debe cerrar sesión específica y retornar 200")
        void debeCerrarSesionEspecificaExitosamente() throws Exception {
            var response = new MessageResponse("SESSION_CLOSED");

            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of("refresh-token"));
            when(tokenService.obtenerJtiRefreshToken(anyString())).thenReturn("current-jti");
            when(tokenService.cerrarSesionEspecifica(anyString(), any(), anyString())).thenReturn(response);

            mockMvc.perform(delete("/auth/sessions/{tokenId}", "target-token-id"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("SESSION_CLOSED"));
        }

        @Test
        @DisplayName("Debe retornar 404 si la sesión no existe")
        void debeRetornar404SesionNoExiste() throws Exception {
            when(cookieService.obtenerRefreshToken(any(HttpServletRequest.class))).thenReturn(Optional.of("refresh-token"));
            when(tokenService.obtenerJtiRefreshToken(anyString())).thenReturn("current-jti");
            when(tokenService.cerrarSesionEspecifica(anyString(), any(), anyString()))
                    .thenThrow(AppException.notFound("SESSION_NOT_FOUND"));

            mockMvc.perform(delete("/auth/sessions/{tokenId}", "nonexistent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("SESSION_NOT_FOUND"));
        }
    }
}
