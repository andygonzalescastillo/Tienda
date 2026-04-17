package com.tienda.backend.repository;

import com.tienda.backend.config.security.AuditConfig;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@DisplayName("UserRepository - Queries de usuarios")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User usuarioActivo;
    private User usuarioInactivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = User.builder()
                .email("activo@mail.com")
                .password("hashed")
                .nombre("Juan")
                .apellido("Pérez")
                .rol(UserRole.USER)
                .emailVerificado(true)
                .estado(true)
                .build();
        usuarioActivo.getAudit().setUsuarioRegistro("test");
        entityManager.persistAndFlush(usuarioActivo);

        usuarioInactivo = User.builder()
                .email("inactivo@mail.com")
                .password("hashed")
                .nombre("María")
                .apellido("García")
                .rol(UserRole.USER)
                .emailVerificado(false)
                .estado(false)
                .build();
        usuarioInactivo.getAudit().setUsuarioRegistro("test");
        entityManager.persistAndFlush(usuarioInactivo);
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("Debe encontrar usuario existente por email")
        void debeEncontrarPorEmail() {
            Optional<User> result = userRepository.findByEmail("activo@mail.com");

            assertThat(result).isPresent();
            assertThat(result.get().getNombre()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("Debe retornar vacío para email inexistente")
        void debeRetornarVacioParaInexistente() {
            Optional<User> result = userRepository.findByEmail("noexiste@mail.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmailAndEstadoTrue")
    class FindByEmailAndEstadoTrue {

        @Test
        @DisplayName("Debe encontrar usuario activo")
        void debeEncontrarUsuarioActivo() {
            Optional<User> result = userRepository.findByEmailAndEstadoTrue("activo@mail.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEstado()).isTrue();
        }

        @Test
        @DisplayName("No debe encontrar usuario inactivo")
        void noDebeEncontrarUsuarioInactivo() {
            Optional<User> result = userRepository.findByEmailAndEstadoTrue("inactivo@mail.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByRolAndEstadoTrue")
    class CountByRolAndEstadoTrue {

        @Test
        @DisplayName("Debe contar solo usuarios activos con el rol especificado")
        void debeContarUsuariosActivosPorRol() {
            long count = userRepository.countByRolAndEstadoTrue(UserRole.USER);

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Debe retornar 0 para roles sin usuarios activos")
        void debeRetornarCeroParaRolSinUsuarios() {
            long count = userRepository.countByRolAndEstadoTrue(UserRole.ADMIN);

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("updateUltimaSesion")
    class UpdateUltimaSesion {

        @Test
        @DisplayName("Debe actualizar la fecha de última sesión")
        void debeActualizarUltimaSesion() {
            Instant ahora = Instant.now().truncatedTo(ChronoUnit.MICROS);

            userRepository.updateUltimaSesion(usuarioActivo.getId(), ahora);
            entityManager.clear();

            User updated = userRepository.findById(usuarioActivo.getId()).orElseThrow();
            assertThat(updated.getUltimaSesion()).isEqualTo(ahora);
        }
    }
}
