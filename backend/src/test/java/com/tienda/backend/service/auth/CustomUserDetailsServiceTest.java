package com.tienda.backend.service.auth;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.entity.UserProvider;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("Debe cargar usuario local con password exitosamente")
    void debeCargarUsuarioLocalExitosamente() {
        var user = User.builder()
                .email("test@mail.com")
                .password("hashedpass123")
                .nombre("Juan")
                .rol(UserRole.USER)
                .estado(true)
                .build();
        user.addProvider(AuthProvider.LOCAL, null);

        when(usuarioRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("test@mail.com");

        assertThat(result.getUsername()).isEqualTo("test@mail.com");
        assertThat(result.getPassword()).isEqualTo("hashedpass123");
        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Debe cargar usuario ADMIN correctamente")
    void debeCargarUsuarioAdmin() {
        var user = User.builder()
                .email("admin@mail.com")
                .password("adminpass123")
                .rol(UserRole.ADMIN)
                .estado(true)
                .build();
        user.addProvider(AuthProvider.LOCAL, null);

        when(usuarioRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("admin@mail.com");

        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    void debeLanzarExcepcionUsuarioNoExiste() {
        when(usuarioRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("noexiste@mail.com"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("USER_NOT_FOUND");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario está inactivo")
    void debeLanzarExcepcionUsuarioInactivo() {
        var user = User.builder()
                .email("inactivo@mail.com")
                .password("pass123")
                .estado(false)
                .build();

        when(usuarioRepository.findByEmail("inactivo@mail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.loadUserByUsername("inactivo@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("USER_INACTIVE");
    }

    @Test
    @DisplayName("Debe lanzar excepción si es cuenta OAuth sin password")
    void debeLanzarExcepcionCuentaOAuthSinPassword() {
        var user = User.builder()
                .email("oauth@mail.com")
                .password(null)
                .estado(true)
                .build();

        var googleProvider = UserProvider.builder()
                .provider(AuthProvider.GOOGLE)
                .providerId("google-id-123")
                .user(user)
                .build();
        user.setProviders(Set.of(googleProvider));

        when(usuarioRepository.findByEmail("oauth@mail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.loadUserByUsername("oauth@mail.com"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("OAUTH_ACCOUNT");
    }

    @Test
    @DisplayName("Debe cargar usuario con password aunque tenga provider OAuth")
    void debeCargarUsuarioConPasswordYOAuth() {
        var user = User.builder()
                .email("dual@mail.com")
                .password("localpass123")
                .rol(UserRole.USER)
                .estado(true)
                .build();

        var googleProvider = UserProvider.builder()
                .provider(AuthProvider.GOOGLE)
                .providerId("google-id-456")
                .user(user)
                .build();
        user.setProviders(Set.of(googleProvider));

        when(usuarioRepository.findByEmail("dual@mail.com")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("dual@mail.com");

        assertThat(result.getUsername()).isEqualTo("dual@mail.com");
        assertThat(result.getPassword()).isEqualTo("localpass123");
    }

    @Test
    @DisplayName("Debe manejar password null con provider LOCAL")
    void debeManejarPasswordNullConProviderLocal() {
        var user = User.builder()
                .email("local-nopass@mail.com")
                .password(null)
                .rol(UserRole.USER)
                .estado(true)
                .build();
        user.addProvider(AuthProvider.LOCAL, null);

        when(usuarioRepository.findByEmail("local-nopass@mail.com")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("local-nopass@mail.com");

        assertThat(result.getPassword()).isEmpty();
    }
}
