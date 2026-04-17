package com.tienda.backend.service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeoLocationService")
class GeoLocationServiceTest {

    private final GeoLocationService service = new GeoLocationService();

    @Nested
    @DisplayName("obtenerUbicacion - IP null o vacía")
    class IpNulaOVacia {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("Debe retornar 'Ubicación desconocida' para IP null, vacía o en blanco")
        void debeRetornarUbicacionDesconocida(String ip) {
            assertThat(service.obtenerUbicacion(ip)).isEqualTo("Ubicación desconocida");
        }
    }

    @Nested
    @DisplayName("obtenerUbicacion - IPs locales")
    class IpsLocales {

        @ParameterizedTest
        @ValueSource(strings = {
                "127.0.0.1",
                "192.168.1.1",
                "192.168.0.100",
                "10.0.0.1",
                "10.255.255.255",
                "172.16.0.1",
                "172.31.255.255",
                "0:0:0:0:0:0:0:1",
                "localhost"
        })
        @DisplayName("Debe retornar ubicación de desarrollo para IPs locales")
        void debeRetornarUbicacionDesarrolloParaIpLocal(String ip) {
            assertThat(service.obtenerUbicacion(ip)).isEqualTo("Lima - Peru (Desarrollo)");
        }
    }

    @Nested
    @DisplayName("obtenerUbicacion - IP externa")
    class IpExterna {

        @Test
        @DisplayName("Debe retornar ubicación o 'Ubicación desconocida' para IP externa")
        void debeRetornarResultadoParaIpExterna() {
            String resultado = service.obtenerUbicacion("8.8.8.8");

            assertThat(resultado).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Debe manejar IP inválida sin lanzar excepción")
        void debeManejarIpInvalidaSinExcepcion() {
            String resultado = service.obtenerUbicacion("999.999.999.999");
            assertThat(resultado).isNotNull();
        }
    }
}
