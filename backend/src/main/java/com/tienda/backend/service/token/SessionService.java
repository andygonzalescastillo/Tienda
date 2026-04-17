package com.tienda.backend.service.token;

import com.tienda.backend.config.properties.JwtProperties;
import com.tienda.backend.domain.entity.RefreshToken;
import com.tienda.backend.domain.entity.User;
import com.tienda.backend.dto.auth.response.SessionResponse;
import com.tienda.backend.dto.websocket.WebSocketMessageDto;
import com.tienda.backend.exception.AppException;
import com.tienda.backend.mapper.SessionMapper;
import com.tienda.backend.repository.RefreshTokenRepository;
import com.tienda.backend.security.jwt.JwtUtils;
import com.tienda.backend.service.util.DeviceDetector;
import com.tienda.backend.service.util.GeoLocationService;
import com.tienda.backend.service.websocket.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepo;
    private final JwtUtils jwtUtilidad;
    private final DeviceDetector deviceDetector;
    private final SessionMapper sessionMapper;
    private final GeoLocationService geoLocationService;
    private final NotificationService notificationService;

    @Transactional
    public void guardarRefreshToken(String token, User user, String ipAddress, String userAgent) {
        gestionarLimiteSesiones(user.getId());

        String ubicacion = geoLocationService.obtenerUbicacion(ipAddress);

        var entity = RefreshToken.builder()
                .tokenId(jwtUtilidad.obtenerJti(token))
                .user(user)
                .tokenHash(generarHashToken(token))
                .fechaExpiracion(jwtUtilidad.obtenerFechaExpiracion(token).toInstant())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .ubicacion(ubicacion)
                .build();
        refreshTokenRepo.save(entity);
    }

    @Transactional
    public void actualizarRefreshToken(RefreshToken entity, String nuevoToken) {
        entity.setTokenId(jwtUtilidad.obtenerJti(nuevoToken));
        entity.setTokenHash(generarHashToken(nuevoToken));
        entity.setFechaExpiracion(jwtUtilidad.obtenerFechaExpiracion(nuevoToken).toInstant());
        refreshTokenRepo.save(entity);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> obtenerSesionesActivas(Long userId, String currentJti) {
        return refreshTokenRepo.findByUserIdAndRevocadoFalseOrderByFechaCreacionDesc(userId).stream()
                .filter(t -> t.getFechaExpiracion().isAfter(Instant.now()))
                .map(t -> {
                    var info = deviceDetector.detectar(t.getUserAgent());
                    return sessionMapper.toResponse(t, info, t.getTokenId().equals(currentJti));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> buscarOpcionalPorTokenId(String tokenId) {
        return refreshTokenRepo.findByTokenIdAndRevocadoFalse(tokenId);
    }

    @Transactional
    public RefreshToken buscarPorTokenId(String tokenId) {
        var token = refreshTokenRepo.findByTokenIdAndRevocadoFalse(tokenId)
                .orElseThrow(() -> AppException.unauthorized("TOKEN_REVOKED"));
        
        token.setUltimoAcceso(Instant.now());
        return refreshTokenRepo.save(token);
    }

    @Transactional
    public void revocarRefreshTokenEntity(RefreshToken t, String razon) {
        t.setRevocado(true);
        t.setFechaRevocacion(Instant.now());
        t.setRazonRevocacion(razon);
        refreshTokenRepo.save(t);
    }

    @Transactional
    public void revocarTodosPorUsuario(Long usuarioId, String razon) {
        refreshTokenRepo.revocarTodosPorUsuario(usuarioId, Instant.now(), razon);
    }

    private void gestionarLimiteSesiones(Long userId) {
        var ahora = Instant.now();
        long activas = refreshTokenRepo.contarSesionesActivas(userId, ahora);

        if (activas >= jwtProperties.maxSessionsPerUser()) {
            var sesiones = refreshTokenRepo.obtenerSesionesActivasOrdenadasPorAntiguedad(userId, ahora);
            if (!sesiones.isEmpty()) {
                var sesionRevocada = sesiones.getFirst();
                revocarRefreshTokenEntity(sesionRevocada, "LIMITE_SESIONES");

                String email = sesionRevocada.getUser().getEmail();
                notificationService.enviarAUsuario(email, NotificationService.SESSION_EVENTS_QUEUE,
                        WebSocketMessageDto.of("SESSION_REVOKED", sesionRevocada.getTokenId()));
            }
        }
    }

    private String generarHashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException _) {
            throw AppException.internalError("CRYPTO_CONFIG_ERROR");
        }
    }
}