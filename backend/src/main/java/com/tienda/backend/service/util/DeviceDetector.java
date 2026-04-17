package com.tienda.backend.service.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DeviceDetector {

    private static final Pattern BRAVE = Pattern.compile("Brave/(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EDGE = Pattern.compile("Edg(?:e|A|iOS)?/(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OPERA = Pattern.compile("(?:OPR|Opera)/(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAMSUNG = Pattern.compile("SamsungBrowser/(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FIREFOX = Pattern.compile("Firefox/(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHROME = Pattern.compile("Chrome/(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFARI = Pattern.compile("Version/(\\S+).*Safari", Pattern.CASE_INSENSITIVE);

    private static final Pattern TABLET = Pattern.compile("iPad|Android(?!.*Mobile)|Tablet|Silk|Kindle|PlayBook", Pattern.CASE_INSENSITIVE);
    private static final Pattern MOBILE = Pattern.compile("Mobile|iPhone|iPod|Android.*Mobile|Windows Phone|BlackBerry|BB10|Opera Mini|IEMobile", Pattern.CASE_INSENSITIVE);
    private static final Pattern BOT = Pattern.compile("bot|crawl|spider|slurp|mediapartners|Googlebot|Bingbot|Yahoo|DuckDuck|Baidu|yandex", Pattern.CASE_INSENSITIVE);

    public DeviceInfo detectar(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return new DeviceInfo("Desconocido", "DESKTOP");
        }

        String navegador = detectarNavegador(userAgentString);
        String so = detectarSistemaOperativo(userAgentString);
        String tipoDispositivo = detectarTipoDispositivo(userAgentString);

        return new DeviceInfo(navegador + " en " + so, tipoDispositivo);
    }

    private String detectarNavegador(String ua) {
        if (BRAVE.matcher(ua).find()) return "Brave";
        if (EDGE.matcher(ua).find()) return "Edge";
        if (OPERA.matcher(ua).find()) return "Opera";
        if (SAMSUNG.matcher(ua).find()) return "Samsung Internet";
        if (FIREFOX.matcher(ua).find()) return "Firefox";
        if (CHROME.matcher(ua).find()) return "Chrome";
        if (SAFARI.matcher(ua).find()) return "Safari";
        return "Desconocido";
    }

    private String detectarSistemaOperativo(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS X") || ua.contains("Macintosh")) return "macOS";
        if (ua.contains("CrOS")) return "ChromeOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Desconocido";
    }

    private String detectarTipoDispositivo(String ua) {
        if (BOT.matcher(ua).find()) return "BOT";
        if (TABLET.matcher(ua).find()) return "TABLET";
        if (MOBILE.matcher(ua).find()) return "MOBILE";
        return "DESKTOP";
    }

    public record DeviceInfo(String descripcion, String tipo) {}
}