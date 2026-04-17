package com.tienda.backend.service.auth;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.AuthProvider;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> AppException.unauthorized("USER_NOT_FOUND"));

        if (!usuario.getEstado()) {
            throw new UsernameNotFoundException("USER_INACTIVE");
        }

        validarCuentaLocal(usuario);

        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(Objects.requireNonNullElse(usuario.getPassword(), ""))
                .roles(usuario.getRol().name())
                .build();
    }

    private void validarCuentaLocal(User usuario) {
        if (usuario.hasProvider(AuthProvider.LOCAL) || StringUtils.hasText(usuario.getPassword())) {
            return;
        }

        String providerName = usuario.getProviders().stream()
                .map(p -> p.getProvider().name())
                .findFirst()
                .orElse("OAuth");

        throw AppException.unauthorized("OAUTH_ACCOUNT", Map.of("provider", providerName));
    }
}