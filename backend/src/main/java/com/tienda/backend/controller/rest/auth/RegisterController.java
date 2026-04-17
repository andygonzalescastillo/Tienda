package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.auth.request.EmailRequest;
import com.tienda.backend.dto.auth.request.RegisterRequest;
import com.tienda.backend.dto.auth.request.VerifyCodeRequest;
import com.tienda.backend.dto.auth.response.LoginResponse;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.RegisterResponse;
import com.tienda.backend.dto.auth.response.VerifyEmailExistenceResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.service.auth.RegistrationService;
import com.tienda.backend.service.util.ClientMetadataService;
import com.tienda.backend.service.util.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegisterController {

    private final RegistrationService registrationService;
    private final CookieService cookieService;
    private final ClientMetadataService clientMetadataService;

    @PostMapping("/verificar-email")
    public ResponseEntity<VerifyEmailExistenceResponse> verificarEmail(@Valid @RequestBody EmailRequest request) {
        VerifyEmailExistenceResponse response = registrationService.verificarEmailExistente(request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registrarUsuario(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = registrationService.registrarUsuarioSinVerificar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verificar-codigo")
    public ResponseEntity<LoginResponse> verificarCodigo(@Valid @RequestBody VerifyCodeRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        var clientInfo = clientMetadataService.extraerClientInfo(httpRequest);
        LoginResult result = registrationService.verificarEmailYGenerarTokens(request, clientInfo.ip(), clientInfo.userAgent());
        cookieService.agregarAccessTokenCookie(httpResponse, result.accessToken());
        cookieService.agregarRefreshTokenCookie(httpResponse, result.refreshToken());
        return ResponseEntity.ok(result.toResponse());
    }

    @PostMapping("/reenviar-codigo")
    public ResponseEntity<MessageResponse> reenviarCodigo(@Valid @RequestBody EmailRequest request) {
        MessageResponse response = registrationService.reenviarCodigo(request.email());
        return ResponseEntity.ok(response);
    }
}
