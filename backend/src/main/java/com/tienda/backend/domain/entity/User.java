package com.tienda.backend.domain.entity;

import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Entity
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    String email;

    @Column(name = "password", length = 255)
    String password;

    @Column(name = "nombre", length = 100)
    String nombre;

    @Column(name = "apellido", length = 100)
    String apellido;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    Set<UserProvider> providers = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 50)
    @Builder.Default
    UserRole rol = UserRole.USER;

    @Column(name = "email_verificado", nullable = false)
    @Builder.Default
    Boolean emailVerificado = false;

    @Embedded
    @Builder.Default
    AuditMetadata audit = new AuditMetadata();

    @Column(name = "estado", nullable = false)
    @Builder.Default
    Boolean estado = false;

    @Column(name = "ultima_sesion")
    Instant ultimaSesion;

    public String getNombreCompleto() {
        var partes = Stream.of(nombre, apellido)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .toList();
        var completo = String.join(" ", partes);
        return completo.isBlank() ? "Sin nombre" : completo;
    }

    public void addProvider(AuthProvider provider, String providerId) {
        var existingProvider = this.providers.stream()
                .filter(p -> p.getProvider() == provider)
                .findFirst();

        if (existingProvider.isPresent()) {
            existingProvider.get().setProviderId(providerId);
        } else {
            var newProvider = UserProvider.builder()
                    .provider(provider)
                    .providerId(providerId)
                    .user(this)
                    .build();
            this.providers.add(newProvider);
        }
    }

    public boolean hasProvider(AuthProvider provider) {
        return this.providers.stream()
                .map(UserProvider::getProvider)
                .anyMatch(p -> p == provider);
    }
}