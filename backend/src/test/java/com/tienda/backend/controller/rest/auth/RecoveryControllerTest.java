package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.exception.ManejadorExcepcionesREST;
import com.tienda.backend.security.jwt.JwtAuthenticationFilter;
import com.tienda.backend.service.auth.PasswordRecoveryService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RecoveryController.class,
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
@DisplayName("RecoveryController - Endpoints de recuperación")
class RecoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordRecoveryService passwordRecoveryService;

    @Nested
    @DisplayName("POST /auth/solicitar-recuperacion")
    class PostSolicitarRecuperacion {

        @Test
        @DisplayName("Debe solicitar recuperación y retornar 200")
        void debeSolicitarRecuperacionExitosamente() throws Exception {
            var response = new MessageResponse("OTP_SENT", Map.of("expirationMinutes", 5));

            when(passwordRecoveryService.solicitarRecuperacionPassword(anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/solicitar-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("OTP_SENT"))
                    .andExpect(jsonPath("$.metadata.expirationMinutes").value(5));
        }

        @Test
        @DisplayName("Debe retornar 200 incluso si el email no existe (seguridad)")
        void debeRetornar200EmailNoExiste() throws Exception {
            var response = new MessageResponse("OTP_SENT", Map.of("expirationMinutes", 5));

            when(passwordRecoveryService.solicitarRecuperacionPassword(anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/solicitar-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "noexiste@mail.com"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("OTP_SENT"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/solicitar-recuperacion")
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
            mockMvc.perform(post("/auth/solicitar-recuperacion")
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
            mockMvc.perform(post("/auth/solicitar-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/verificar-codigo-recuperacion")
    class PostVerificarCodigoRecuperacion {

        @Test
        @DisplayName("Debe verificar código y retornar 200")
        void debeVerificarCodigoExitosamente() throws Exception {
            var response = new MessageResponse("CODE_VERIFIED");

            when(passwordRecoveryService.verificarCodigoRecuperacion(anyString(), anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/verificar-codigo-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "123456"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("CODE_VERIFIED"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el código ha expirado")
        void debeRetornar400CodigoExpirado() throws Exception {
            when(passwordRecoveryService.verificarCodigoRecuperacion(anyString(), anyString()))
                    .thenThrow(AppException.badRequest("VERIFICATION_CODE_EXPIRED", Map.of()));

            mockMvc.perform(post("/auth/verificar-codigo-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "000000"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VERIFICATION_CODE_EXPIRED"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el código es inválido")
        void debeRetornar400CodigoInvalido() throws Exception {
            when(passwordRecoveryService.verificarCodigoRecuperacion(anyString(), anyString()))
                    .thenThrow(AppException.badRequest("INVALID_VERIFICATION_CODE", Map.of()));

            mockMvc.perform(post("/auth/verificar-codigo-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "999999"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_VERIFICATION_CODE"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "", "codigo": "123456"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el código no tiene 6 dígitos")
        void debeRetornar400CodigoCorto() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "12"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body está vacío")
        void debeRetornar400BodyVacio() throws Exception {
            mockMvc.perform(post("/auth/verificar-codigo-recuperacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/restablecer-password")
    class PostRestablecerPassword {

        private static final String VALID_BODY = """
                {
                    "email": "test@mail.com",
                    "codigo": "123456",
                    "nuevaPassword": "NuevaPass123"
                }
                """;

        @Test
        @DisplayName("Debe restablecer contraseña y retornar 200")
        void debeRestablecerPasswordExitosamente() throws Exception {
            var response = new MessageResponse("PASSWORD_UPDATED");

            when(passwordRecoveryService.restablecerPassword(anyString(), anyString(), anyString())).thenReturn(response);

            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("PASSWORD_UPDATED"));
        }

        @Test
        @DisplayName("Debe retornar 404 si el usuario no existe")
        void debeRetornar404UsuarioNoExiste() throws Exception {
            when(passwordRecoveryService.restablecerPassword(anyString(), anyString(), anyString()))
                    .thenThrow(AppException.notFound("USER_NOT_FOUND"));

            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email está vacío")
        void debeRetornar400SinEmail() throws Exception {
            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "", "codigo": "123456", "nuevaPassword": "NuevaPass123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el email tiene formato inválido")
        void debeRetornar400EmailInvalido() throws Exception {
            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "no-es-email", "codigo": "123456", "nuevaPassword": "NuevaPass123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el código no tiene 6 dígitos")
        void debeRetornar400CodigoCorto() throws Exception {
            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "12", "nuevaPassword": "NuevaPass123"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si la contraseña es muy corta")
        void debeRetornar400PasswordCorta() throws Exception {
            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "123456", "nuevaPassword": "Ab1"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si la contraseña no tiene números")
        void debeRetornar400PasswordSinNumeros() throws Exception {
            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@mail.com", "codigo": "123456", "nuevaPassword": "SoloLetras"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body está vacío")
        void debeRetornar400BodyVacio() throws Exception {
            mockMvc.perform(post("/auth/restablecer-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
