package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.auth.request.LoginRequest;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.SessionValidationResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.exception.ManejadorExcepcionesREST;
import com.tienda.backend.security.jwt.JwtAuthenticationFilter;
import com.tienda.backend.service.auth.AuthenticationService;
import com.tienda.backend.service.util.ClientMetadataService;
import com.tienda.backend.service.util.CookieService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LoginController.class,
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
@DisplayName("LoginController - POST /auth/login")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private ClientMetadataService clientMetadataService;

    @Nested
    @DisplayName("POST /auth/login")
    class PostLogin {

        @Test
        @DisplayName("Debe autenticar con credenciales válidas y retornar 200")
        void debeAutenticarConCredencialesValidas() throws Exception {
            var clientInfo = new ClientMetadataService.ClientInfo("127.0.0.1", "Mozilla");
            var loginResult = new LoginResult("access-jwt", "refresh-jwt", "LOGIN_SUCCESS", "test@mail.com", "Juan", "USER");

            when(clientMetadataService.extraerClientInfo(any(HttpServletRequest.class))).thenReturn(clientInfo);
            when(authenticationService.autenticarUsuario(any(LoginRequest.class), anyString(), anyString())).thenReturn(loginResult);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "password": "Password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("LOGIN_SUCCESS"))
                    .andExpect(jsonPath("$.email").value("test@mail.com"))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.rol").value("USER"));

            verify(cookieService).eliminarCookies(any());
            verify(cookieService).agregarAccessTokenCookie(any(), anyString());
            verify(cookieService).agregarRefreshTokenCookie(any(), anyString());
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "", "password": "Password123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email tiene formato inválido")
        void debeRetornar400EmailInvalido() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "no-es-email", "password": "Password123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si la contraseña está vacía")
        void debeRetornar400SinPassword() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "password": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 401 con credenciales inválidas")
        void debeRetornar401CredencialesInvalidas() throws Exception {
            var clientInfo = new ClientMetadataService.ClientInfo("127.0.0.1", "Mozilla");
            when(clientMetadataService.extraerClientInfo(any(HttpServletRequest.class))).thenReturn(clientInfo);
            when(authenticationService.autenticarUsuario(any(LoginRequest.class), anyString(), anyString()))
                    .thenThrow(AppException.unauthorized("INVALID_CREDENTIALS"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "password": "WrongPass123"}
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body está vacío")
        void debeRetornar400BodyVacio() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /auth/validate")
    class GetValidate {

        @Test
        @DisplayName("Debe validar sesión activa y retornar 200")
        void debeValidarSesionActiva() throws Exception {
            var response = new SessionValidationResponse("test@mail.com", "Juan", "USER");

            when(cookieService.obtenerAccessToken(any(HttpServletRequest.class))).thenReturn(Optional.of("access-jwt"));
            when(authenticationService.validarSesionActual(any(), anyString())).thenReturn(response);

            mockMvc.perform(get("/auth/validate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@mail.com"))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.rol").value("USER"));
        }

        @Test
        @DisplayName("Debe retornar 401 si no hay cookie de access token")
        void debeRetornar401SinCookie() throws Exception {
            when(cookieService.obtenerAccessToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

            mockMvc.perform(get("/auth/validate"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("SESSION_NOT_FOUND"));
        }
    }
}
