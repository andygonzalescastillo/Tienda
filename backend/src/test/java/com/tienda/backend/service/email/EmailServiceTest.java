package com.tienda.backend.service.email;

import com.tienda.backend.config.properties.MailCustomProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService - Envío de emails via Brevo API")
class EmailServiceTest {

    @Mock private SpringTemplateEngine templateEngine;
    @Mock private MailCustomProperties mailCustomProperties;

    @InjectMocks
    private EmailService service;

    @BeforeEach
    void setUp() throws Exception {
        // Inyectar brevoApiKey via reflexión (campo @Value no lo inyecta Mockito)
        Field field = EmailService.class.getDeclaredField("brevoApiKey");
        field.setAccessible(true);
        field.set(service, "test-api-key-for-brevo");
    }

    @Nested
    @DisplayName("enviarCodigoVerificacion")
    class EnviarCodigoVerificacion {

        @Test
        @DisplayName("Debe procesar template 'email-verificacion' con variables correctas")
        void debeProcesarTemplateVerificacion() {
            var contextCaptor = ArgumentCaptor.forClass(Context.class);

            when(templateEngine.process(eq("email-verificacion"), contextCaptor.capture()))
                    .thenReturn("<html>Código: 123456</html>");
            when(mailCustomProperties.senderName()).thenReturn("Mi Tienda");
            when(mailCustomProperties.username()).thenReturn("noreply@tienda.com");

            // El HTTP call a Brevo fallará en test (no hay servidor real),
            // pero el error es capturado internamente sin propagar
            assertThatCode(() -> service.enviarCodigoVerificacion("user@mail.com", "123456", 5))
                    .doesNotThrowAnyException();

            verify(templateEngine).process(eq("email-verificacion"), any(Context.class));

            var context = contextCaptor.getValue();
            assertThat(context.getVariableNames()).contains("codigo", "tiempo");
            assertThat(context.getVariable("codigo")).isEqualTo("123456");
            assertThat(context.getVariable("tiempo")).isEqualTo(5L);
        }

        @Test
        @DisplayName("No debe lanzar excepción aunque falle el envío HTTP")
        void noDebeLanzarExcepcionSiFallaEnvio() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenReturn("<html>Test</html>");
            when(mailCustomProperties.senderName()).thenReturn("Mi Tienda");
            when(mailCustomProperties.username()).thenReturn("noreply@tienda.com");

            assertThatCode(() -> service.enviarCodigoVerificacion("user@mail.com", "123", 5))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("enviarCodigoRecuperacion")
    class EnviarCodigoRecuperacion {

        @Test
        @DisplayName("Debe procesar template 'email-recuperacion' con variables correctas")
        void debeProcesarTemplateRecuperacion() {
            var contextCaptor = ArgumentCaptor.forClass(Context.class);

            when(templateEngine.process(eq("email-recuperacion"), contextCaptor.capture()))
                    .thenReturn("<html>Recupera tu cuenta</html>");
            when(mailCustomProperties.senderName()).thenReturn("Mi Tienda");
            when(mailCustomProperties.username()).thenReturn("noreply@tienda.com");

            assertThatCode(() -> service.enviarCodigoRecuperacion("user@mail.com", "654321", 15))
                    .doesNotThrowAnyException();

            verify(templateEngine).process(eq("email-recuperacion"), any(Context.class));

            var context = contextCaptor.getValue();
            assertThat(context.getVariable("codigo")).isEqualTo("654321");
            assertThat(context.getVariable("tiempo")).isEqualTo(15L);
        }
    }

    @Nested
    @DisplayName("Procesamiento de templates")
    class ProcesamientoTemplates {

        @Test
        @DisplayName("Debe usar templates distintos para verificación y recuperación")
        void debeUsarTemplatesDistintos() {
            when(templateEngine.process(anyString(), any(Context.class)))
                    .thenReturn("<html>Test</html>");
            when(mailCustomProperties.senderName()).thenReturn("Mi Tienda");
            when(mailCustomProperties.username()).thenReturn("noreply@tienda.com");

            service.enviarCodigoVerificacion("a@mail.com", "111", 5);
            service.enviarCodigoRecuperacion("b@mail.com", "222", 10);

            verify(templateEngine).process(eq("email-verificacion"), any(Context.class));
            verify(templateEngine).process(eq("email-recuperacion"), any(Context.class));
        }
    }
}
