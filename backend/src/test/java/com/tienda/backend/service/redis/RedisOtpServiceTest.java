package com.tienda.backend.service.redis;

import com.tienda.backend.config.properties.SecurityProperties;
import com.tienda.backend.domain.enums.VerificationCodeType;
import com.tienda.backend.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisOtpService - Gestión de códigos OTP")
class RedisOtpServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SecurityProperties securityProps;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private RedisOtpService redisOtpService;

    private SecurityProperties.Otp otpConfig;
    private final String email = "test@mail.com";
    private final VerificationCodeType tipo = VerificationCodeType.REGISTRO;
    private final String key = "OTP:test@mail.com:REGISTRO";

    @BeforeEach
    void setUp() {
        otpConfig = new SecurityProperties.Otp(5, 3);
    }

    @Nested
    @DisplayName("guardarCodigo")
    class GuardarCodigo {

        @Test
        @DisplayName("Debe guardar código en Redis con expiración")
        void debeGuardarCodigoEnRedis() {
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(securityProps.otp()).thenReturn(otpConfig);

            redisOtpService.guardarCodigo(email, "123456", tipo);

            verify(hashOperations).putAll(eq(key), anyMap());
            verify(redisTemplate).expire(eq(key), any());
        }
    }

    @Nested
    @DisplayName("validarCodigo")
    class ValidarCodigo {

        @Test
        @DisplayName("Código correcto no debe lanzar excepción y debe eliminar la key")
        void codigoCorrectoNoLanzaExcepcion() {
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.get(key, "codigo")).thenReturn("123456");

            assertThatCode(() -> redisOtpService.validarCodigo(email, "123456", tipo))
                    .doesNotThrowAnyException();

            verify(redisTemplate).delete(key);
        }

        @Test
        @DisplayName("Código correcto con eliminarAlValidar=false no debe eliminar la key")
        void codigoCorrectoSinEliminar() {
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.get(key, "codigo")).thenReturn("123456");

            assertThatCode(() -> redisOtpService.validarCodigo(email, "123456", tipo, false))
                    .doesNotThrowAnyException();

            verify(redisTemplate, never()).delete(key);
        }

        @Test
        @DisplayName("Código expirado (no existe en Redis) debe lanzar VERIFICATION_CODE_EXPIRED")
        void codigoExpiradoLanzaError() {
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.get(key, "codigo")).thenReturn(null);
            when(securityProps.otp()).thenReturn(otpConfig);

            assertThatThrownBy(() -> redisOtpService.validarCodigo(email, "123456", tipo))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("VERIFICATION_CODE_EXPIRED");
                    });
        }

        @Test
        @DisplayName("Código incorrecto debe lanzar INVALID_VERIFICATION_CODE con intentos restantes")
        void codigoIncorrectoLanzaError() {
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.get(key, "codigo")).thenReturn("999999");
            when(hashOperations.increment(key, "intentos", 1)).thenReturn(1L);
            when(securityProps.otp()).thenReturn(otpConfig);

            assertThatThrownBy(() -> redisOtpService.validarCodigo(email, "123456", tipo))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("INVALID_VERIFICATION_CODE");
                    });
        }

        @Test
        @DisplayName("Exceder máximo de intentos debe lanzar MAX_ATTEMPTS_EXCEEDED y eliminar código")
        void maxIntentosExcedidosLanzaError() {
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.get(key, "codigo")).thenReturn("999999");
            when(hashOperations.increment(key, "intentos", 1)).thenReturn(3L);
            when(securityProps.otp()).thenReturn(otpConfig);

            assertThatThrownBy(() -> redisOtpService.validarCodigo(email, "123456", tipo))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo("MAX_ATTEMPTS_EXCEEDED");
                    });

            verify(redisTemplate).delete(key);
        }
    }

    @Nested
    @DisplayName("generarCodigo")
    class GenerarCodigo {

        @Test
        @DisplayName("Debe generar código de 6 dígitos, guardar en Redis y publicar evento")
        void debeGenerarCodigoYPublicarEvento() {
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(securityProps.otp()).thenReturn(otpConfig);

            redisOtpService.generarCodigo(email, tipo);

            verify(redisTemplate).delete(key);
            verify(hashOperations).putAll(eq(key), anyMap());
            verify(eventPublisher).publishEvent(any(Object.class));
        }
    }

    @Nested
    @DisplayName("eliminarCodigo")
    class EliminarCodigo {

        @Test
        @DisplayName("Debe eliminar la key de Redis")
        void debeEliminarKey() {
            redisOtpService.eliminarCodigo(email, tipo);

            verify(redisTemplate).delete(key);
        }
    }
}
