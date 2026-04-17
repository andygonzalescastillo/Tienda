package com.tienda.backend.service.email;

import com.tienda.backend.config.properties.MailCustomProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SpringTemplateEngine templateEngine;
    private final MailCustomProperties mailCustomProperties;

    @Value("${spring.mail.password}")
    private String brevoApiKey;

    @Async
    public void enviarCodigoVerificacion(String email, String codigo, long minutos) {
        enviarEmailTemplate(email, codigo, minutos, "email-verificacion", "Código de verificación");
    }

    @Async
    public void enviarCodigoRecuperacion(String email, String codigo, long minutos) {
        enviarEmailTemplate(email, codigo, minutos, "email-recuperacion", "Recuperación de contraseña");
    }

    private void enviarEmailTemplate(String email, String codigo, long minutos, String templateName, String subject) {
        Map<String, Object> variables = Map.of(
                "codigo", codigo,
                "tiempo", minutos
        );
        String htmlContent = procesarPlantilla(templateName, variables);
        enviarCorreoHttp(email, subject, htmlContent);
    }

    private String procesarPlantilla(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    private void enviarCorreoHttp(String email, String asunto, String contenidoHtml) {
        try {
            String requestBody = """
                    {
                      "sender": {"name": "%s", "email": "%s"},
                      "to": [{"email": "%s"}],
                      "subject": "%s",
                      "htmlContent": "%s"
                    }
                    """.formatted(
                    escaparJson(mailCustomProperties.senderName()),
                    escaparJson(mailCustomProperties.username()),
                    escaparJson(email),
                    escaparJson(asunto),
                    escaparJson(contenidoHtml)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email enviado exitosamente a {}", email);
            } else {
                log.error("Error enviando email a {} mediante Brevo API: HTTP {} - {}", email, response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Excepción enviando email a {}: {}", email, e.getMessage());
        }
    }

    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}