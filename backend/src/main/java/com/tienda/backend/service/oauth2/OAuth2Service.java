package com.tienda.backend.service.oauth2;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import com.tienda.backend.service.util.EmailNormalizer;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository usuarioRepository;

    @Transactional
    public User procesarUsuarioOAuth2(String email, String nombre, String apellido, String proveedor, String providerId) {
        var emailNorm = EmailNormalizer.normalize(email);

        if (!StringUtils.hasText(providerId)) {
            throw AppException.unauthorized("OAUTH2_PROVIDER_ID_MISSING");
        }

        AuthProvider providerEnum;
        try {
            providerEnum = AuthProvider.valueOf(proveedor.toUpperCase().trim());
        } catch (IllegalArgumentException _) {
            throw AppException.unauthorized("OAUTH2_AUTHENTICATION_FAILED");
        }

        return usuarioRepository.findByEmail(emailNorm)
                .map(user -> actualizarUsuario(user, providerEnum, providerId))
                .orElseGet(() -> crearUsuario(emailNorm, nombre, apellido, providerEnum, providerId));
    }

    private User actualizarUsuario(User user, AuthProvider provider, String providerId) {
        if (!user.hasProvider(provider)) {
            user.addProvider(provider, providerId);
        } else {
            user.getProviders().stream()
                    .filter(p -> p.getProvider() == provider && !providerId.equals(p.getProviderId()))
                    .findFirst()
                    .ifPresent(p -> p.setProviderId(providerId));
        }

        if (!user.getEmailVerificado()) user.setEmailVerificado(true);
        if (!user.getEstado()) user.setEstado(true);

        user.setUltimaSesion(Instant.now());
        user.getAudit().setUsuarioUltimaModificacion("OAUTH_" + provider.name());

        return usuarioRepository.save(user);
    }

    private User crearUsuario(String email, String nombre, String apellido, AuthProvider provider, String providerId) {
        var newUser = User.builder()
                .email(email)
                .nombre(StringUtils.hasText(nombre) ? nombre.trim() : "")
                .apellido(StringUtils.hasText(apellido) ? apellido.trim() : "")
                .emailVerificado(true)
                .rol(UserRole.USER)
                .estado(true)
                .ultimaSesion(Instant.now())
                .build();

        newUser.getAudit().setUsuarioRegistro("OAUTH_" + provider.name());
        newUser.addProvider(provider, providerId);

        return usuarioRepository.save(newUser);
    }
}