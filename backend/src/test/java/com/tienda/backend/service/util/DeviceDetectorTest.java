package com.tienda.backend.service.util;

import com.tienda.backend.service.util.DeviceDetector.DeviceInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeviceDetector - Detección de navegador, SO y tipo de dispositivo")
class DeviceDetectorTest {

    private final DeviceDetector detector = new DeviceDetector();

    @Nested
    @DisplayName("Detección de navegador")
    class DeteccionNavegador {

        @Test
        @DisplayName("Debe detectar Chrome en Windows")
        void debeDetectarChromeWindows() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).contains("Chrome");
            assertThat(info.descripcion()).contains("Windows");
            assertThat(info.tipo()).isEqualTo("DESKTOP");
        }

        @Test
        @DisplayName("Debe detectar Firefox en Linux")
        void debeDetectarFirefoxLinux() {
            String ua = "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/121.0";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).contains("Firefox");
            assertThat(info.descripcion()).contains("Linux");
        }

        @Test
        @DisplayName("Debe detectar Safari en macOS")
        void debeDetectarSafariMacOS() {
            String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_2_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).contains("Safari");
            assertThat(info.descripcion()).contains("macOS");
        }

        @Test
        @DisplayName("Debe detectar Edge en Windows")
        void debeDetectarEdgeWindows() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).contains("Edge");
            assertThat(info.descripcion()).contains("Windows");
        }

        @Test
        @DisplayName("Debe detectar Opera")
        void debeDetectarOpera() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 OPR/106.0.0.0";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).contains("Opera");
        }

        @Test
        @DisplayName("Debe detectar Samsung Internet")
        void debeDetectarSamsungInternet() {
            String ua = "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/23.0 Chrome/115.0.0.0 Mobile Safari/537.36";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).contains("Samsung Internet");
        }
    }

    @Nested
    @DisplayName("Detección de tipo de dispositivo")
    class DeteccionTipoDispositivo {

        @Test
        @DisplayName("Debe detectar dispositivo móvil iPhone")
        void debeDetectarMobileIphone() {
            String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.tipo()).isEqualTo("MOBILE");
            // El SO se detecta como iOS porque contiene "iPhone"
            assertThat(info.descripcion()).contains("Safari");
        }

        @Test
        @DisplayName("Debe detectar tablet iPad")
        void debeDetectarTabletIpad() {
            String ua = "Mozilla/5.0 (iPad; CPU OS 17_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.tipo()).isEqualTo("TABLET");
        }

        @Test
        @DisplayName("Debe detectar dispositivo Android Mobile")
        void debeDetectarAndroidMobile() {
            String ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.tipo()).isEqualTo("MOBILE");
            assertThat(info.descripcion()).contains("Android");
        }

        @Test
        @DisplayName("Debe detectar tablet Android (sin 'Mobile')")
        void debeDetectarAndroidTablet() {
            String ua = "Mozilla/5.0 (Linux; Android 13; SM-X710) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.tipo()).isEqualTo("TABLET");
        }

        @Test
        @DisplayName("Debe detectar bot/crawler")
        void debeDetectarBot() {
            String ua = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.tipo()).isEqualTo("BOT");
        }

        @Test
        @DisplayName("Debe retornar DESKTOP para ChromeOS")
        void debeDetectarChromeOS() {
            String ua = "Mozilla/5.0 (X11; CrOS x86_64 14541.0.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).contains("ChromeOS");
            assertThat(info.tipo()).isEqualTo("DESKTOP");
        }
    }

    @Nested
    @DisplayName("Casos edge")
    class CasosEdge {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Debe retornar 'Desconocido' y DESKTOP para user-agent nulo, vacío o en blanco")
        void debeRetornarDesconocidoParaUAInvalido(String ua) {
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).isEqualTo("Desconocido");
            assertThat(info.tipo()).isEqualTo("DESKTOP");
        }

        @Test
        @DisplayName("Debe retornar 'Desconocido en Desconocido' para user-agent no reconocido")
        void debeRetornarDesconocidoParaUANoReconocido() {
            String ua = "CustomApp/1.0";
            DeviceInfo info = detector.detectar(ua);

            assertThat(info.descripcion()).isEqualTo("Desconocido en Desconocido");
            assertThat(info.tipo()).isEqualTo("DESKTOP");
        }
    }
}
