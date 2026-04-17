package com.tienda.backend.service.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class ClientMetadataService {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern IPV6_PATTERN = Pattern.compile("^[0-9a-fA-F:]+$");
    private static final Set<String> LOCAL_IPS = Set.of("0:0:0:0:0:0:0:1", "::1", "127.0.0.1");

    public record ClientInfo(String ip, String userAgent) {}

    public ClientInfo extraerClientInfo(HttpServletRequest req) {
        return new ClientInfo(
                extraerIpReal(req),
                req.getHeader("User-Agent")
        );
    }

    public String extraerIpReal(HttpServletRequest req) {
        var remoteAddr = req.getRemoteAddr();

        if (!esIpPrivadaParaProxy(remoteAddr)) {
            return normalizarIp(remoteAddr);
        }

        var forwardedFor = req.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(forwardedFor)) {
            return normalizarIp(remoteAddr);
        }

        return Stream.of(forwardedFor.split(","))
                .map(String::trim)
                .filter(this::esIpPublicaValida)
                .findFirst()
                .map(this::normalizarIp)
                .orElse(normalizarIp(remoteAddr));
    }

    private String normalizarIp(String ip) {
        if (!StringUtils.hasText(ip)) return "IP desconocida";
        if (LOCAL_IPS.contains(ip)) return "127.0.0.1";

        if (ip.contains(".") && ip.contains(":")) {
            return ip.substring(ip.lastIndexOf(":") + 1);
        }
        return ip;
    }

    private boolean esIpPrivadaParaProxy(String ip) {
        if (!StringUtils.hasText(ip)) return false;
        return ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("172.") || ip.startsWith("127.") || LOCAL_IPS.contains(ip);
    }

    private boolean esIpPublicaValida(String ip) {
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) return false;

        return (IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches())
                && !ip.startsWith("10.")
                && !ip.startsWith("192.168.")
                && !ip.startsWith("127.");
    }
}