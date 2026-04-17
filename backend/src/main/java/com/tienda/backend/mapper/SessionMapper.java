package com.tienda.backend.mapper;

import com.tienda.backend.domain.entity.RefreshToken;
import com.tienda.backend.dto.auth.response.SessionResponse;
import com.tienda.backend.service.util.DeviceDetector.DeviceInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface SessionMapper {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "tokenId", source = "entity.tokenId")
    @Mapping(target = "usuarioId", source = "entity.user.id")
    @Mapping(target = "fechaCreacion", source = "entity.fechaCreacion")
    @Mapping(target = "fechaExpiracion", source = "entity.fechaExpiracion")
    @Mapping(target = "ipAddress", source = "entity.ipAddress")
    @Mapping(target = "userAgent", source = "entity.userAgent")
    @Mapping(target = "ubicacion", source = "entity.ubicacion")
    @Mapping(target = "revocado", source = "entity.revocado")
    @Mapping(target = "nombreDispositivo", source = "info.descripcion")
    @Mapping(target = "tipoDispositivo", source = "info.tipo")
    @Mapping(target = "ultimoAcceso", source = "entity.ultimoAcceso")
    @Mapping(target = "esActual", source = "esActual")
    SessionResponse toResponse(RefreshToken entity, DeviceInfo info, boolean esActual);
}