package com.tienda.backend;

import com.tienda.backend.config.properties.AdminProperties;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run( String @NonNull ... args) {
        gestionarAdminInicial();
    }

    private void gestionarAdminInicial() {
        long adminsActuales = usuarioRepository.countByRolAndEstadoTrue(UserRole.ADMIN);
        if (adminsActuales > 0) {
            log.info("Sistema iniciado con {} administrador(es) activo(s)", adminsActuales);
            return;
        }

        var admin = crearInstanciaAdmin();
        usuarioRepository.save(admin);
        log.info("Super administrador creado: {} ({})", admin.getNombre(), admin.getEmail());
    }

    private User crearInstanciaAdmin() {
        var admin = User.builder()
                .email(adminProperties.email())
                .password(passwordEncoder.encode(adminProperties.password()))
                .nombre(adminProperties.nombre() != null ? adminProperties.nombre() : "Super")
                .apellido(adminProperties.apellido() != null ? adminProperties.apellido() : "Admin")
                .rol(UserRole.ADMIN)
                .emailVerificado(true)
                .estado(true)
                .build();

        admin.addProvider(AuthProvider.LOCAL, "Auth");
        admin.getAudit().setUsuarioRegistro("SISTEMA");

        return admin;
    }
}