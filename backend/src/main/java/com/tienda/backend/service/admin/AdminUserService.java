package com.tienda.backend.service.admin;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.admin.request.ChangeEstadoRequest;
import com.tienda.backend.dto.admin.request.ChangeRolRequest;
import com.tienda.backend.dto.admin.response.AdminUserResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.mapper.AdminUserMapper;
import com.tienda.backend.repository.UserRepository;
import com.tienda.backend.service.websocket.NotificationService;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminUserMapper adminUserMapper;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> listarUsuarios(String search, UserRole rol, Boolean estado, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank()) ? "%" + search.trim().toLowerCase() + "%" : null;
        return userRepository.buscarUsuarios(searchParam, rol, estado, pageable)
                .map(adminUserMapper::toResponse);
    }

    @Transactional
    public MessageResponse cambiarRol(Long userId, ChangeRolRequest request, String adminEmail) {
        User user = buscarUsuarioPorId(userId);

        validarNoEsElMismo(user, adminEmail, "No puedes cambiar tu propio rol");

        if (user.getRol() == request.rol()) {
            throw AppException.conflict("USER_ALREADY_HAS_ROLE");
        }

        if (request.rol() == UserRole.ADMIN && !tienePasswordLocal(user)) {
            throw AppException.badRequest("USER_NO_LOCAL_ACCOUNT", Map.of());
        }

        user.setRol(request.rol());
        userRepository.save(user);

        notificationService.enviarATodosLosAdmins(WebSocketMessageDto.of("USERS_UPDATED"));

        return new MessageResponse("USER_ROLE_UPDATED");
    }

    @Transactional
    public MessageResponse cambiarEstado(Long userId, ChangeEstadoRequest request, String adminEmail) {
        User user = buscarUsuarioPorId(userId);

        validarNoEsElMismo(user, adminEmail, "No puedes cambiar tu propio estado");

        if (user.getEstado().equals(request.estado())) {
            throw AppException.conflict("USER_ALREADY_HAS_STATUS");
        }

        user.setEstado(request.estado());
        userRepository.save(user);

        notificationService.enviarATodosLosAdmins(WebSocketMessageDto.of("USERS_UPDATED"));

        return new MessageResponse(request.estado() ? "USER_ACTIVATED" : "USER_DEACTIVATED");
    }

    private User buscarUsuarioPorId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("USER_NOT_FOUND"));
    }

    private void validarNoEsElMismo(User user, String adminEmail, String mensaje) {
        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw AppException.conflict("SELF_MODIFICATION_NOT_ALLOWED");
        }
    }

    private boolean tienePasswordLocal(User user) {
        return user.getPassword() != null && !user.getPassword().isBlank();
    }
}
