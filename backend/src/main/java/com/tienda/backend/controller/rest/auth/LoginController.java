package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.auth.request.LoginRequest;
import com.tienda.backend.dto.auth.response.LoginResponse;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.SessionValidationResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.service.auth.AuthenticationService;
import com.tienda.backend.service.util.ClientMetadataService;
import com.tienda.backend.service.util.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationService authenticationService;
    private final CookieService cookieService;
    private final ClientMetadataService clientMetadataService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(@Valid @RequestBody LoginRequest request,
                                                        HttpServletRequest httpRequest,
                                                        HttpServletResponse httpResponse) {
        var clientInfo = clientMetadataService.extraerClientInfo(httpRequest);
        LoginResult result = authenticationService.autenticarUsuario(request, clientInfo.ip(), clientInfo.userAgent());

        cookieService.eliminarCookies(httpResponse);
        cookieService.agregarAccessTokenCookie(httpResponse, result.accessToken());
        cookieService.agregarRefreshTokenCookie(httpResponse, result.refreshToken());

        return ResponseEntity.ok(result.toResponse());
    }

    @GetMapping("/validate")
    public ResponseEntity<SessionValidationResponse> validarSesionActual(@AuthenticationPrincipal String email,
                                                                          HttpServletRequest request) {
        String accessToken = cookieService.obtenerAccessToken(request)
                .orElseThrow(() -> AppException.unauthorized("SESSION_NOT_FOUND"));

        return ResponseEntity.ok(authenticationService.validarSesionActual(email, accessToken));
    }
}
