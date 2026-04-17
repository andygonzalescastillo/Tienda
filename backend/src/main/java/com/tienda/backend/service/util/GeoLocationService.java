package com.tienda.backend.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class GeoLocationService {

    private final RestClient restClient;

    public GeoLocationService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://ip-api.com")
                .build();
    }

    public String obtenerUbicacion(String ip) {
        if (ip == null || ip.isBlank()) {
            return "Ubicación desconocida";
        }
        
        if (isLocalIp(ip)) {
            return "Lima - Peru (Desarrollo)";
        }

        try {
            Map<String, Object> response = restClient.get()
                    .uri("/json/{ip}", ip)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            if (response != null && "success".equals(response.get("status"))) {
                String city = (String) response.get("city");
                String country = (String) response.get("country");
                return city + " - " + country;
            }
        } catch (Exception e) {
            log.warn("Error al obtener ubicación para IP {}: {}", ip, e.getMessage());
        }
        
        return "Ubicación desconocida";
    }

    private boolean isLocalIp(String ip) {
        return ip.startsWith("127.") || ip.startsWith("192.168.") || 
               ip.startsWith("10.") || ip.startsWith("172.") || 
               ip.equals("0:0:0:0:0:0:0:1") || ip.equals("localhost");
    }
}
