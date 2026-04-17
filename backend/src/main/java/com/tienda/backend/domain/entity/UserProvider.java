package com.tienda.backend.domain.entity;

import com.tienda.backend.domain.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_providers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_provider", columnNames = {"user_id", "provider_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_name", nullable = false, length = 20)
    @EqualsAndHashCode.Include
    AuthProvider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    String providerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    User user;
}