package com.tienda.backend.security.oauth.strategy;

import com.tienda.backend.domain.enums.AuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OAuth2 Strategies")
class   OAuth2StrategiesTest {

    private OAuth2User crearUsuario(Map<String, Object> attrs, String nameKey) {
        return new DefaultOAuth2User(Collections.emptyList(), attrs, nameKey);
    }

    @Nested
    @DisplayName("GoogleStrategy")
    class Google {

        private final GoogleStrategy strategy = new GoogleStrategy();

        @Test
        @DisplayName("Debe retornar AuthProvider.GOOGLE")
        void debeRetornarProviderGoogle() {
            assertThat(strategy.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        }

        @Test
        @DisplayName("Debe extraer datos de usuario Google")
        void debeExtraerDatos() {
            var user = crearUsuario(Map.of(
                    "sub", "google-123",
                    "email", "user@gmail.com",
                    "given_name", "Juan",
                    "family_name", "Pérez"
            ), "sub");

            assertThat(strategy.getEmail(user)).isEqualTo("user@gmail.com");
            assertThat(strategy.getFirstName(user)).isEqualTo("Juan");
            assertThat(strategy.getLastName(user)).isEqualTo("Pérez");
            assertThat(strategy.getProviderId(user)).isEqualTo("google-123");
        }

        @Test
        @DisplayName("Debe retornar vacío si atributos no existen")
        void debeRetornarVacioSiNoExisten() {
            var user = crearUsuario(Map.of("sub", "123"), "sub");
            assertThat(strategy.getEmail(user)).isEmpty();
            assertThat(strategy.getFirstName(user)).isEmpty();
            assertThat(strategy.getLastName(user)).isEmpty();
        }
    }

    @Nested
    @DisplayName("FacebookStrategy")
    class Facebook {

        private final FacebookStrategy strategy = new FacebookStrategy();

        @Test
        @DisplayName("Debe retornar AuthProvider.FACEBOOK")
        void debeRetornarProviderFacebook() {
            assertThat(strategy.getProvider()).isEqualTo(AuthProvider.FACEBOOK);
        }

        @Test
        @DisplayName("Debe extraer datos de usuario Facebook")
        void debeExtraerDatos() {
            var user = crearUsuario(Map.of(
                    "id", "fb-456",
                    "email", "user@facebook.com",
                    "first_name", "María",
                    "last_name", "García"
            ), "id");

            assertThat(strategy.getEmail(user)).isEqualTo("user@facebook.com");
            assertThat(strategy.getFirstName(user)).isEqualTo("María");
            assertThat(strategy.getLastName(user)).isEqualTo("García");
            assertThat(strategy.getProviderId(user)).isEqualTo("fb-456");
        }

        @Test
        @DisplayName("Debe usar 'name' como fallback si 'first_name' está vacío")
        void debeUsarNameComoFallback() {
            var user = crearUsuario(Map.of("id", "fb-789", "name", "Carlos López"), "id");

            assertThat(strategy.getFirstName(user)).isEqualTo("Carlos López");
        }
    }

    @Nested
    @DisplayName("GithubStrategy")
    class Github {

        private final GithubStrategy strategy = new GithubStrategy();

        @Test
        @DisplayName("Debe retornar AuthProvider.GITHUB")
        void debeRetornarProviderGithub() {
            assertThat(strategy.getProvider()).isEqualTo(AuthProvider.GITHUB);
        }

        @Test
        @DisplayName("Debe extraer datos de usuario GitHub con nombre completo")
        void debeExtraerDatosConNombreCompleto() {
            var user = crearUsuario(Map.of(
                    "id", 12345,
                    "email", "dev@github.com",
                    "name", "Pedro Sánchez",
                    "login", "pedros"
            ), "id");

            assertThat(strategy.getEmail(user)).isEqualTo("dev@github.com");
            assertThat(strategy.getFirstName(user)).isEqualTo("Pedro");
            assertThat(strategy.getLastName(user)).isEqualTo("Sánchez");
            assertThat(strategy.getProviderId(user)).isEqualTo("12345");
        }

        @Test
        @DisplayName("Debe usar 'login' como fallback si 'name' está vacío")
        void debeUsarLoginComoFallback() {
            var user = crearUsuario(Map.of("id", 999, "login", "devuser"), "id");

            assertThat(strategy.getFirstName(user)).isEqualTo("devuser");
            assertThat(strategy.getLastName(user)).isEmpty();
        }

        @Test
        @DisplayName("Debe extraer email del array 'emails' si email directo está vacío")
        void debeExtraerEmailDeArrayEmails() {
            var attrs = new HashMap<String, Object>();
            attrs.put("id", 111);
            attrs.put("emails", List.of(Map.of("email", "hidden@github.com")));
            var user = crearUsuario(attrs, "id");

            assertThat(strategy.getEmail(user)).isEqualTo("hidden@github.com");
        }

        @Test
        @DisplayName("Debe manejar nombre sin apellido")
        void debeManejarNombreSinApellido() {
            var user = crearUsuario(Map.of("id", 222, "name", "Ana"), "id");

            assertThat(strategy.getFirstName(user)).isEqualTo("Ana");
            assertThat(strategy.getLastName(user)).isEmpty();
        }
    }

    @Nested
    @DisplayName("MicrosoftStrategy")
    class Microsoft {

        private final MicrosoftStrategy strategy = new MicrosoftStrategy();

        @Test
        @DisplayName("Debe retornar AuthProvider.MICROSOFT")
        void debeRetornarProviderMicrosoft() {
            assertThat(strategy.getProvider()).isEqualTo(AuthProvider.MICROSOFT);
        }

        @Test
        @DisplayName("Debe extraer datos de usuario Microsoft")
        void debeExtraerDatos() {
            var user = crearUsuario(Map.of(
                    "sub", "ms-001",
                    "email", "user@outlook.com",
                    "givenName", "Luis",
                    "surname", "Ramírez"
            ), "sub");

            assertThat(strategy.getEmail(user)).isEqualTo("user@outlook.com");
            assertThat(strategy.getFirstName(user)).isEqualTo("Luis");
            assertThat(strategy.getLastName(user)).isEqualTo("Ramírez");
            assertThat(strategy.getProviderId(user)).isEqualTo("ms-001");
        }

        @Test
        @DisplayName("Debe usar 'mail' como fallback si 'email' no existe")
        void debeUsarMailComoFallback() {
            var user = crearUsuario(Map.of("sub", "ms-002", "mail", "alt@microsoft.com"), "sub");

            assertThat(strategy.getEmail(user)).isEqualTo("alt@microsoft.com");
        }

        @Test
        @DisplayName("Debe usar 'displayName' como fallback para firstName")
        void debeUsarDisplayNameComoFallback() {
            var user = crearUsuario(Map.of("sub", "ms-003", "displayName", "Ana Torres"), "sub");

            assertThat(strategy.getFirstName(user)).isEqualTo("Ana Torres");
        }

        @Test
        @DisplayName("Debe usar 'id' como fallback para providerId si 'sub' no existe")
        void debeUsarIdComoFallbackParaProviderId() {
            var user = crearUsuario(Map.of("id", "ms-alt-id"), "id");

            assertThat(strategy.getProviderId(user)).isEqualTo("ms-alt-id");
        }
    }
}
