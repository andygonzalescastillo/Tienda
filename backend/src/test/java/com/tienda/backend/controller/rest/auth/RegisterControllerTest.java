package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.auth.request.RegisterRequest;
import com.tienda.backend.dto.auth.request.VerifyCodeRequest;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.RegisterResponse;
import com.tienda.backend.dto.auth.response.VerifyEmailExistenceResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.exception.ManejadorExcepcionesREST;
import com.tienda.backend.security.jwt.JwtAuthenticationFilter;
import com.tienda.backend.service.auth.RegistrationService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RegisterController.class,
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
@DisplayName("RegisterController - Endpoints de registro")
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private ClientMetadataService clientMetadataService;

    @Nested
    @DisplayName("POST /auth/verificar-email")
    class PostVerificarEmail {

        @Test
        @DisplayName("Debe retornar 200 cuando el email existe con contraseña local")
        void debeRetornar200EmailExisteConPassword() throws Exception {
            var response = new VerifyEmailExistenceResponse(true, true, "LOCAL", List.of("LOCAL"));

            when(registrationService.verificarEmailExistente(anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/verificar-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.existe").value(true))
                    .andExpect(jsonPath("$.tienePassword").value(true))
                    .andExpect(jsonPath("$.proveedor").value("LOCAL"))
                    .andExpect(jsonPath("$.proveedoresVinculados[0]").value("LOCAL"));
        }

        @Test
        @DisplayName("Debe retornar 200 cuando el email no existe")
        void debeRetornar200EmailNoExiste() throws Exception {
            var response = new VerifyEmailExistenceResponse(false, false, null, null);

            when(registrationService.verificarEmailExistente(anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/verificar-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "nuevo@mail.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.existe").value(false))
                    .andExpect(jsonPath("$.tienePassword").value(false));
        }

        @Test
        @DisplayName("Debe retornar 200 cuando el email existe con proveedor OAuth")
        void debeRetornar200EmailExisteConOAuth() throws Exception {
            var response = new VerifyEmailExistenceResponse(true, false, "GOOGLE", List.of("GOOGLE"));

            when(registrationService.verificarEmailExistente(anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/verificar-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "oauth@mail.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.existe").value(true))
                    .andExpect(jsonPath("$.tienePassword").value(false))
                    .andExpect(jsonPath("$.proveedor").value("GOOGLE"))
                    .andExpect(jsonPath("$.proveedoresVinculados[0]").value("GOOGLE"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/verificar-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email tiene formato inválido")
        void debeRetornar400EmailInvalido() throws Exception {
            mockMvc.perform(post("/auth/verificar-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "no-es-email"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body está vacío")
        void debeRetornar400BodyVacio() throws Exception {
            mockMvc.perform(post("/auth/verificar-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/register")
    class PostRegister {

        private static final String VALID_REGISTER_BODY = """
                {
                    "email": "nuevo@mail.com",
                    "password": "Password123",
                    "nombre": "Juan",
                    "apellido": "Pérez"
                }
                """;

        @Test
        @DisplayName("Debe registrar usuario y retornar 201")
        void debeRegistrarUsuarioExitosamente() throws Exception {
            var response = new RegisterResponse("nuevo@mail.com", "Juan");

            when(registrationService.registrarUsuarioSinVerificar(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_REGISTER_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("nuevo@mail.com"))
                    .andExpect(jsonPath("$.nombre").value("Juan"));
        }

        @Test
        @DisplayName("Debe retornar 409 si el email ya está verificado")
        void debeRetornar409EmailYaVerificado() throws Exception {
            when(registrationService.registrarUsuarioSinVerificar(any(RegisterRequest.class)))
                    .thenThrow(AppException.conflict("EMAIL_ALREADY_VERIFIED"));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_REGISTER_BODY))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_VERIFIED"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "", "password": "Password123", "nombre": "Juan", "apellido": "Pérez"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email tiene formato inválido")
        void debeRetornar400EmailInvalido() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "no-es-email", "password": "Password123", "nombre": "Juan", "apellido": "Pérez"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si la contraseña es muy corta")
        void debeRetornar400PasswordCorta() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "password": "Ab1", "nombre": "Juan", "apellido": "Pérez"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si la contraseña no tiene números")
        void debeRetornar400PasswordSinNumeros() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "password": "SoloLetras", "nombre": "Juan", "apellido": "Pérez"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el nombre está vacío")
        void debeRetornar400SinNombre() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "password": "Password123", "nombre": "", "apellido": "Pérez"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el apellido está vacío")
        void debeRetornar400SinApellido() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "password": "Password123", "nombre": "Juan", "apellido": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body está vacío")
        void debeRetornar400BodyVacio() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/verificar-codigo")
    class PostVerificarCodigo {

        @Test
        @DisplayName("Debe verificar código y retornar 200 con tokens")
        void debeVerificarCodigoExitosamente() throws Exception {
            var clientInfo = new ClientMetadataService.ClientInfo("127.0.0.1", "Mozilla");
            var loginResult = new LoginResult("access-jwt", "refresh-jwt", "LOGIN_SUCCESS", "test@mail.com", "Juan", "USER");

            when(clientMetadataService.extraerClientInfo(any(HttpServletRequest.class))).thenReturn(clientInfo);
            when(registrationService.verificarEmailYGenerarTokens(any(VerifyCodeRequest.class), anyString(), anyString())).thenReturn(loginResult);

            mockMvc.perform(post("/auth/verificar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "123456"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("LOGIN_SUCCESS"))
                    .andExpect(jsonPath("$.email").value("test@mail.com"))
                    .andExpect(jsonPath("$.nombre").value("Juan"))
                    .andExpect(jsonPath("$.rol").value("USER"));

            verify(cookieService).agregarAccessTokenCookie(any(), anyString());
            verify(cookieService).agregarRefreshTokenCookie(any(), anyString());
        }

        @Test
        @DisplayName("Debe retornar 404 si el usuario no existe")
        void debeRetornar404UsuarioNoExiste() throws Exception {
            var clientInfo = new ClientMetadataService.ClientInfo("127.0.0.1", "Mozilla");
            when(clientMetadataService.extraerClientInfo(any(HttpServletRequest.class))).thenReturn(clientInfo);
            when(registrationService.verificarEmailYGenerarTokens(any(VerifyCodeRequest.class), anyString(), anyString()))
                    .thenThrow(AppException.notFound("USER_NOT_FOUND"));

            mockMvc.perform(post("/auth/verificar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "noexiste@mail.com", "codigo": "123456"}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "", "codigo": "123456"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email tiene formato inválido")
        void debeRetornar400EmailInvalido() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "no-es-email", "codigo": "123456"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el código está vacío")
        void debeRetornar400SinCodigo() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el código no tiene 6 dígitos")
        void debeRetornar400CodigoCorto() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body está vacío")
        void debeRetornar400BodyVacio() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/reenviar-codigo")
    class PostReenviarCodigo {

        @Test
        @DisplayName("Debe reenviar código y retornar 200")
        void debeReenviarCodigoExitosamente() throws Exception {
            var response = new MessageResponse("CODE_RESENT", Map.of("expirationMinutes", 5));

            when(registrationService.reenviarCodigo(anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/reenviar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("CODE_RESENT"))
                    .andExpect(jsonPath("$.metadata.expirationMinutes").value(5));
        }

        @Test
        @DisplayName("Debe retornar 409 si el email ya está verificado o no existe")
        void debeRetornar409EmailError() throws Exception {
            when(registrationService.reenviarCodigo(anyString()))
                    .thenThrow(AppException.conflict("EMAIL_ERROR"));

            mockMvc.perform(post("/auth/reenviar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "verificado@mail.com"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("EMAIL_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/reenviar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email tiene formato inválido")
        void debeRetornar400EmailInvalido() throws Exception {
            mockMvc.perform(post("/auth/reenviar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "no-es-email"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body está vacío")
        void debeRetornar400BodyVacio() throws Exception {
            mockMvc.perform(post("/auth/reenviar-codigo")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
