package com.tienda.backend.mapper;

import com.tienda.backend.domain.entity.User;
import com.tienda.backend.domain.entity.UserProvider;
import com.tienda.backend.dto.admin.response.AdminUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface AdminUserMapper {

    @Mapping(target = "providers", source = "providers", qualifiedByName = "mapProviders")
    @Mapping(target = "fechaRegistro", source = "audit.fechaRegistro")
    AdminUserResponse toResponse(User user);

    @Named("mapProviders")
    default Set<String> mapProviders(Set<UserProvider> providers) {
        if (providers == null) return Set.of();
        return providers.stream()
                .map(UserProvider::getProvider)
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}
