package com.tienda.backend.controller.rest.auth;

import com.tienda.backend.dto.auth.response.LoginResponse;
import com.tienda.backend.dto.auth.response.LoginResult;
import com.tienda.backend.dto.auth.response.SessionResponse;
import com.tienda.backend.dto.common.MessageResponse;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.service.token.TokenService;
import com.tienda.backend.service.util.ClientMetadataService;
import com.tienda.backend.service.util.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SessionController {

    private final TokenService tokenService;
    private final CookieService cookieService;
    private final ClientMetadataService clientMetadataService;

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refrescarToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.obtenerRefreshToken(request)
                .orElseThrow(() -> AppException.unauthorized("REFRESH_TOKEN_NOT_FOUND"));

        var clientInfo = clientMetadataService.extraerClientInfo(request);
        LoginResult result = tokenService.refrescarToken(refreshToken, clientInfo.ip(), clientInfo.userAgent());

        cookieService.agregarAccessTokenCookie(response, result.accessToken());
        cookieService.agregarRefreshTokenCookie(response, result.refreshToken());

        return ResponseEntity.ok(result.toResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> cerrarSesion(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = cookieService.obtenerAccessToken(request).orElse(null);
        String refreshToken = cookieService.obtenerRefreshToken(request).orElse(null);

        tokenService.cerrarSesion(accessToken, refreshToken);

        cookieService.eliminarCookies(response);
        limpiarHeadersCache(response);

        return ResponseEntity.ok(new MessageResponse("LOGOUT_SUCCESS"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<MessageResponse> cerrarTodasLasSesiones(HttpServletRequest request,
                                                                    HttpServletResponse response,
                                                                    @AuthenticationPrincipal String email) {
        cookieService.obtenerAccessToken(request)
                .ifPresent(token -> tokenService.revocarAccessToken(token, "LOGOUT_ALL"));

        tokenService.cerrarTodasLasSesionesDelUsuario(email);
        cookieService.eliminarCookies(response);
        limpiarHeadersCache(response);

        return ResponseEntity.ok(new MessageResponse("LOGOUT_ALL_SUCCESS"));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> obtenerSesionesActivas(HttpServletRequest request,
                                                                          @AuthenticationPrincipal String email) {
        String currentJti = tokenService.obtenerJtiRefreshToken(
                cookieService.obtenerRefreshToken(request).orElse(null));

        return ResponseEntity.ok(tokenService.obtenerSesionesActivas(email, currentJti));
    }

    @DeleteMapping("/sessions/{tokenId}")
    public ResponseEntity<MessageResponse> cerrarSesionEspecifica(@PathVariable String tokenId,
                                                                    HttpServletRequest request,
                                                                    @AuthenticationPrincipal String email) {
        String currentJti = tokenService.obtenerJtiRefreshToken(cookieService.obtenerRefreshToken(request).orElse(null));

        MessageResponse msg = tokenService.cerrarSesionEspecifica(tokenId, email, currentJti);

        return ResponseEntity.ok(msg);
    }

    @GetMapping("/ws-token")
    public ResponseEntity<MessageResponse> obtenerWsToken(HttpServletRequest request) {
        String accessToken = cookieService.obtenerAccessToken(request)
                .orElseThrow(() -> AppException.unauthorized("SESSION_NOT_FOUND"));
        return ResponseEntity.ok(new MessageResponse(accessToken));
    }

    private void limpiarHeadersCache(HttpServletResponse res) {
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        res.setHeader(HttpHeaders.PRAGMA, "no-cache");
        res.setHeader(HttpHeaders.EXPIRES, "0");
    }
}
