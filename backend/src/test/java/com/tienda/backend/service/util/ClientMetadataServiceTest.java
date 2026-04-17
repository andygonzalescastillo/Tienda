package com.tienda.backend.service.util;

import com.tienda.backend.service.util.ClientMetadataService.ClientInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientMetadataService")
class ClientMetadataServiceTest {

    @Mock private HttpServletRequest request;

    private final ClientMetadataService service = new ClientMetadataService();

    @Nested
    @DisplayName("extraerClientInfo")
    class ExtraerClientInfo {

        @Test
        @DisplayName("Debe retornar IP y User-Agent del request")
        void debeRetornarIpYUserAgent() {
            when(request.getRemoteAddr()).thenReturn("200.100.50.25");
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Chrome/120");

            ClientInfo info = service.extraerClientInfo(request);

            assertThat(info.ip()).isEqualTo("200.100.50.25");
            assertThat(info.userAgent()).isEqualTo("Mozilla/5.0 Chrome/120");
        }
    }

    @Nested
    @DisplayName("extraerIpReal - IP directa")
    class IpDirecta {

        @Test
        @DisplayName("Debe retornar IP directa del request cuando es pública")
        void debeRetornarIpDirecta() {
            when(request.getRemoteAddr()).thenReturn("190.200.100.50");

            assertThat(service.extraerIpReal(request)).isEqualTo("190.200.100.50");
        }

        @Test
        @DisplayName("Debe normalizar IPv6 localhost a 127.0.0.1")
        void debeNormalizarIpv6Localhost() {
            when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");

            assertThat(service.extraerIpReal(request)).isEqualTo("127.0.0.1");
        }

        @Test
        @DisplayName("Debe normalizar ::1 a 127.0.0.1")
        void debeNormalizarIpv6Corto() {
            when(request.getRemoteAddr()).thenReturn("::1");

            assertThat(service.extraerIpReal(request)).isEqualTo("127.0.0.1");
        }

        @Test
        @DisplayName("Debe retornar 'IP desconocida' para IP vacía")
        void debeRetornarDesconocidaParaVacia() {
            when(request.getRemoteAddr()).thenReturn("");

            assertThat(service.extraerIpReal(request)).isEqualTo("IP desconocida");
        }
    }

    @Nested
    @DisplayName("extraerIpReal - Con proxy (IP privada)")
    class ConProxy {

        @Test
        @DisplayName("Debe extraer IP pública del header X-Forwarded-For cuando remoteAddr es privada")
        void debeExtraerIpDeXForwardedFor() {
            when(request.getRemoteAddr()).thenReturn("172.17.0.1");
            when(request.getHeader("X-Forwarded-For")).thenReturn("200.100.50.25, 172.17.0.1");

            assertThat(service.extraerIpReal(request)).isEqualTo("200.100.50.25");
        }

        @Test
        @DisplayName("Debe ignorar IPs privadas en X-Forwarded-For y tomar la pública")
        void debeIgnorarIpsPrivadas() {
            when(request.getRemoteAddr()).thenReturn("172.17.0.1");
            when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1, 45.67.89.10");

            assertThat(service.extraerIpReal(request)).isEqualTo("45.67.89.10");
        }

        @Test
        @DisplayName("Debe usar remoteAddr si X-Forwarded-For está vacío")
        void debeUsarRemoteAddrSiXForwardedForVacio() {
            when(request.getRemoteAddr()).thenReturn("172.17.0.1");
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);

            assertThat(service.extraerIpReal(request)).isEqualTo("172.17.0.1");
        }

        @Test
        @DisplayName("Debe ignorar X-Forwarded-For si remoteAddr no es IP privada")
        void debeIgnorarXForwardedForSiNoEsIpPrivada() {
            when(request.getRemoteAddr()).thenReturn("200.100.50.25");

            assertThat(service.extraerIpReal(request)).isEqualTo("200.100.50.25");
        }

        @Test
        @DisplayName("Debe extraer IP real cuando remoteAddr es 10.x.x.x")
        void debeExtraerIpConProxy10() {
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");
            when(request.getHeader("X-Forwarded-For")).thenReturn("85.120.45.10");

            assertThat(service.extraerIpReal(request)).isEqualTo("85.120.45.10");
        }
    }
}
