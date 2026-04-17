package com.tienda.backend.controller.rest.admin;

import com.tienda.backend.domain.enums.UserRole;
import com.tienda.backend.dto.admin.request.ChangeEstadoRequest;
import com.tienda.backend.dto.admin.request.ChangeRolRequest;
import com.tienda.backend.dto.admin.response.AdminUserResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.service.admin.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> listarUsuarios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole rol,
            @RequestParam(required = false) Boolean estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "audit.fechaRegistro"));
        return ResponseEntity.ok(adminUserService.listarUsuarios(search, rol, estado, pageable));
    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<MessageResponse> cambiarRol(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRolRequest request,
            @AuthenticationPrincipal String adminEmail
    ) {
        return ResponseEntity.ok(adminUserService.cambiarRol(id, request, adminEmail));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<MessageResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ChangeEstadoRequest request,
            @AuthenticationPrincipal String adminEmail
    ) {
        return ResponseEntity.ok(adminUserService.cambiarEstado(id, request, adminEmail));
    }
}
