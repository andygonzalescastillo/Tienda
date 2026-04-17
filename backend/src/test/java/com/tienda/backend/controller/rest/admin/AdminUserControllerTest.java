package com.tienda.backend.controller.rest.admin;

import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.admin.response.AdminUserResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.exception.ManejadorExcepcionesREST;
import com.tienda.backend.security.jwt.JwtAuthenticationFilter;
import com.tienda.backend.service.admin.AdminUserService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminUserController.class,
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
@DisplayName("AdminUserController - /api/admin/usuarios")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @Nested
    @DisplayName("GET /api/admin/usuarios")
    class GetListarUsuarios {

        @Test
        @DisplayName("Debe retornar página de usuarios con 200")
        void debeRetornarPaginaDeUsuarios() throws Exception {
            var response = new AdminUserResponse(1L, "user@mail.com", "Juan", "Pérez",
                    UserRole.USER, true, true, Set.of("LOCAL"), Instant.now(), Instant.now());
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

            when(adminUserService.listarUsuarios(isNull(), isNull(), isNull(), any())).thenReturn(page);

            mockMvc.perform(get("/api/admin/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].email").value("user@mail.com"))
                    .andExpect(jsonPath("$.content[0].nombre").value("Juan"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Debe aceptar parámetros de búsqueda y paginación")
        void debeAceptarParametrosDeBusqueda() throws Exception {
            when(adminUserService.listarUsuarios(anyString(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/admin/usuarios")
                            .param("search", "juan")
                            .param("rol", "USER")
                            .param("estado", "true")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/usuarios/{id}/rol")
    class PatchCambiarRol {

        @Test
        @DisplayName("Debe cambiar rol exitosamente y retornar 200")
        void debeCambiarRolExitosamente() throws Exception {
            when(adminUserService.cambiarRol(eq(1L), any(), any()))
                    .thenReturn(new MessageResponse("USER_ROLE_UPDATED"));

            mockMvc.perform(patch("/api/admin/usuarios/1/rol")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"rol": "ADMIN"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("USER_ROLE_UPDATED"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body no tiene rol")
        void debeRetornar400SinRol() throws Exception {
            mockMvc.perform(patch("/api/admin/usuarios/1/rol")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 409 si admin intenta cambiar su propio rol")
        void debeRetornar409AutoModificacion() throws Exception {
            when(adminUserService.cambiarRol(eq(1L), any(), any()))
                    .thenThrow(AppException.conflict("SELF_MODIFICATION_NOT_ALLOWED"));

            mockMvc.perform(patch("/api/admin/usuarios/1/rol")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"rol": "ADMIN"}
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("SELF_MODIFICATION_NOT_ALLOWED"));
        }

        @Test
        @DisplayName("Debe retornar 400 si usuario no tiene cuenta local para ser ADMIN")
        void debeRetornar400SinCuentaLocal() throws Exception {
            when(adminUserService.cambiarRol(eq(1L), any(), any()))
                    .thenThrow(AppException.badRequest("USER_NO_LOCAL_ACCOUNT", Map.of()));

            mockMvc.perform(patch("/api/admin/usuarios/1/rol")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"rol": "ADMIN"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("USER_NO_LOCAL_ACCOUNT"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/usuarios/{id}/estado")
    class PatchCambiarEstado {

        @Test
        @DisplayName("Debe cambiar estado exitosamente y retornar 200")
        void debeCambiarEstadoExitosamente() throws Exception {
            when(adminUserService.cambiarEstado(eq(1L), any(), any()))
                    .thenReturn(new MessageResponse("USER_DEACTIVATED"));

            mockMvc.perform(patch("/api/admin/usuarios/1/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"estado": false}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.successCode").value("USER_DEACTIVATED"));
        }

        @Test
        @DisplayName("Debe retornar 400 si el body no tiene estado")
        void debeRetornar400SinEstado() throws Exception {
            mockMvc.perform(patch("/api/admin/usuarios/1/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Debe retornar 404 si usuario no existe")
        void debeRetornar404SiNoExiste() throws Exception {
            when(adminUserService.cambiarEstado(eq(99L), any(), any()))
                    .thenThrow(AppException.notFound("USER_NOT_FOUND"));

            mockMvc.perform(patch("/api/admin/usuarios/99/estado")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"estado": false}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
        }
    }
}
