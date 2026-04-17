package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.auth.request.EmailRequest;
import com.tienda.backend.dto.auth.request.ResetPasswordRequest;
import com.tienda.backend.dto.auth.request.VerifyCodeRequest;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.service.auth.PasswordRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<MessageResponse> solicitarRecuperacion(@Valid @RequestBody EmailRequest request) {
        MessageResponse response = passwordRecoveryService.solicitarRecuperacionPassword(request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verificar-codigo-recuperacion")
    public ResponseEntity<MessageResponse> verificarCodigoRecuperacion(@Valid @RequestBody VerifyCodeRequest request) {
        MessageResponse response = passwordRecoveryService.verificarCodigoRecuperacion(request.email(), request.codigo());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<MessageResponse> restablecerPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = passwordRecoveryService.restablecerPassword(request.email(), request.codigo(), request.nuevaPassword());
        return ResponseEntity.ok(response);
    }
}
