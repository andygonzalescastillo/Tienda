package com.tienda.backend.service.admin;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.admin.request.ChangeEstadoRequest;
import com.tienda.backend.dto.admin.request.ChangeRolRequest;
import com.tienda.backend.dto.admin.response.AdminUserResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.mapper.AdminUserMapper;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.service.websocket.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService - Gestión de usuarios por admin")
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AdminUserMapper adminUserMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private AdminUserService adminUserService;

    private User usuarioBase;
    private final String adminEmail = "admin@mail.com";

    @BeforeEach
    void setUp() {
        usuarioBase = User.builder()
                .id(10L)
                .email("usuario@mail.com")
                .password("hashed-password")
                .nombre("Juan")
                .apellido("Pérez")
                .rol(UserRole.USER)
                .emailVerificado(true)
                .estado(true)
                .build();
        usuarioBase.addProvider(AuthProvider.LOCAL, "LOCAL");
    }

    @Nested
    @DisplayName("listarUsuarios")
    class ListarUsuarios {

        @Test
        @DisplayName("Debe retornar página de usuarios mapeados")
        void debeRetornarPaginaDeUsuarios() {
            var pageable = PageRequest.of(0, 10);
            var usersPage = new PageImpl<>(List.of(usuarioBase), pageable, 1);
            var expectedResponse = new AdminUserResponse(10L, "usuario@mail.com", "Juan", "Pérez",
                    UserRole.USER, true, true, java.util.Set.of("LOCAL"), null, null);

            when(userRepository.buscarUsuarios(null, null, null, pageable)).thenReturn(usersPage);
            when(adminUserMapper.toResponse(usuarioBase)).thenReturn(expectedResponse);

            Page<AdminUserResponse> result = adminUserService.listarUsuarios(null, null, null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().email()).isEqualTo("usuario@mail.com");
        }

        @Test
        @DisplayName("Debe aplicar filtro de búsqueda con formato correcto")
        void debeAplicarFiltroDeBusqueda() {
            var pageable = PageRequest.of(0, 10);
            when(userRepository.buscarUsuarios(eq("%juan%"), isNull(), isNull(), eq(pageable)))
                    .thenReturn(Page.empty());

            adminUserService.listarUsuarios("Juan", null, null, pageable);

            verify(userRepository).buscarUsuarios("%juan%", null, null, pageable);
        }

        @Test
        @DisplayName("Debe pasar null como search si el texto está vacío o en blanco")
        void debePasarNullSiSearchVacio() {
            var pageable = PageRequest.of(0, 10);
            when(userRepository.buscarUsuarios(isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(Page.empty());

            adminUserService.listarUsuarios("   ", null, null, pageable);

            verify(userRepository).buscarUsuarios(null, null, null, pageable);
        }
    }

    @Nested
    @DisplayName("cambiarRol")
    class CambiarRol {

        @Test
        @DisplayName("Debe cambiar rol de USER a ADMIN exitosamente")
        void debeCambiarRolExitosamente() {
            var request = new ChangeRolRequest(UserRole.ADMIN);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            MessageResponse response = adminUserService.cambiarRol(10L, request, adminEmail);

            assertThat(response.successCode()).isEqualTo("USER_ROLE_UPDATED");
            assertThat(usuarioBase.getRol()).isEqualTo(UserRole.ADMIN);
            verify(userRepository).save(usuarioBase);
            verify(notificationService).enviarATodosLosAdmins(any());
        }

        @Test
        @DisplayName("Debe lanzar SELF_MODIFICATION_NOT_ALLOWED si admin intenta cambiar su propio rol")
        void debeLanzarErrorAlCambiarPropioRol() {
            usuarioBase.setEmail(adminEmail);
            var request = new ChangeRolRequest(UserRole.ADMIN);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            assertThatThrownBy(() -> adminUserService.cambiarRol(10L, request, adminEmail))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("SELF_MODIFICATION_NOT_ALLOWED");
                        assertThat(appEx.getStatus().value()).isEqualTo(409);
                    });

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar USER_ALREADY_HAS_ROLE si el usuario ya tiene ese rol")
        void debeLanzarErrorSiYaTieneRol() {
            var request = new ChangeRolRequest(UserRole.USER);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            assertThatThrownBy(() -> adminUserService.cambiarRol(10L, request, adminEmail))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("USER_ALREADY_HAS_ROLE");
                    });
        }

        @Test
        @DisplayName("Debe lanzar USER_NO_LOCAL_ACCOUNT si se intenta hacer ADMIN a usuario sin password")
        void debeLanzarErrorSiNoTienePasswordParaAdmin() {
            usuarioBase.setPassword(null);
            var request = new ChangeRolRequest(UserRole.ADMIN);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            assertThatThrownBy(() -> adminUserService.cambiarRol(10L, request, adminEmail))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("USER_NO_LOCAL_ACCOUNT");
                        assertThat(appEx.getStatus().value()).isEqualTo(400);
                    });
        }

        @Test
        @DisplayName("Debe lanzar USER_NOT_FOUND si el usuario no existe")
        void debeLanzarErrorSiUsuarioNoExiste() {
            var request = new ChangeRolRequest(UserRole.ADMIN);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.cambiarRol(99L, request, adminEmail))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("USER_NOT_FOUND");
                        assertThat(appEx.getStatus().value()).isEqualTo(404);
                    });
        }
    }

    @Nested
    @DisplayName("cambiarEstado")
    class CambiarEstado {

        @Test
        @DisplayName("Debe desactivar usuario exitosamente")
        void debeDesactivarUsuarioExitosamente() {
            var request = new ChangeEstadoRequest(false);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            MessageResponse response = adminUserService.cambiarEstado(10L, request, adminEmail);

            assertThat(response.successCode()).isEqualTo("USER_DEACTIVATED");
            assertThat(usuarioBase.getEstado()).isFalse();
            verify(userRepository).save(usuarioBase);
            verify(notificationService).enviarATodosLosAdmins(any());
        }

        @Test
        @DisplayName("Debe activar usuario exitosamente")
        void debeActivarUsuarioExitosamente() {
            usuarioBase.setEstado(false);
            var request = new ChangeEstadoRequest(true);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            MessageResponse response = adminUserService.cambiarEstado(10L, request, adminEmail);

            assertThat(response.successCode()).isEqualTo("USER_ACTIVATED");
            assertThat(usuarioBase.getEstado()).isTrue();
        }

        @Test
        @DisplayName("Debe lanzar SELF_MODIFICATION_NOT_ALLOWED si admin intenta cambiar su propio estado")
        void debeLanzarErrorAlCambiarPropioEstado() {
            usuarioBase.setEmail(adminEmail);
            var request = new ChangeEstadoRequest(false);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            assertThatThrownBy(() -> adminUserService.cambiarEstado(10L, request, adminEmail))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("SELF_MODIFICATION_NOT_ALLOWED");
                    });
        }

        @Test
        @DisplayName("Debe lanzar USER_ALREADY_HAS_STATUS si ya tiene ese estado")
        void debeLanzarErrorSiYaTieneEstado() {
            var request = new ChangeEstadoRequest(true);
            when(userRepository.findById(10L)).thenReturn(Optional.of(usuarioBase));

            assertThatThrownBy(() -> adminUserService.cambiarEstado(10L, request, adminEmail))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("USER_ALREADY_HAS_STATUS");
                    });
        }

        @Test
        @DisplayName("Debe lanzar USER_NOT_FOUND si el usuario no existe")
        void debeLanzarErrorSiUsuarioNoExiste() {
            var request = new ChangeEstadoRequest(false);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminUserService.cambiarEstado(99L, request, adminEmail))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("USER_NOT_FOUND");
                    });
        }
    }
}
