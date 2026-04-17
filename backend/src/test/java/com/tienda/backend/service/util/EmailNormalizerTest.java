package com.tienda.backend.service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailNormalizer - Normalización de emails")
class EmailNormalizerTest {

    @Test
    @DisplayName("Debe convertir email con mayúsculas a minúsculas")
    void debeConvertirMayusculasAMinusculas() {
        String emailConMayusculas = "TEST@MAIL.COM";
        String resultado = EmailNormalizer.normalize(emailConMayusculas);
        assertThat(resultado).isEqualTo("test@mail.com");
    }

    @Test
    @DisplayName("Debe eliminar espacios al inicio y al final del email")
    void debeEliminarEspacios() {
        String emailConEspacios = "  test@mail.com  ";
        String resultado = EmailNormalizer.normalize(emailConEspacios);
        assertThat(resultado).isEqualTo("test@mail.com");
    }

    @Test
    @DisplayName("Debe manejar email con mayúsculas Y espacios al mismo tiempo")
    void debeManejarMayusculasYEspacios() {
        String emailMixto = "  TEST@Mail.COM  ";
        String resultado = EmailNormalizer.normalize(emailMixto);
        assertThat(resultado).isEqualTo("test@mail.com");
    }

    @Test
    @DisplayName("No debe modificar un email que ya está normalizado")
    void noDebeModificarEmailYaNormalizado() {
        String emailNormal = "usuario@gmail.com";
        String resultado = EmailNormalizer.normalize(emailNormal);
        assertThat(resultado).isEqualTo("usuario@gmail.com");
    }

    @Test
    @DisplayName("Debe retornar cadena vacía cuando el email es null")
    void debeRetornarVacioCuandoEsNull() {
        String emailNull = null;
        String resultado = EmailNormalizer.normalize(emailNull);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe manejar cadena vacía sin errores")
    void debeManejarCadenaVacia() {
        String emailVacio = "";
        String resultado = EmailNormalizer.normalize(emailVacio);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe manejar cadena con solo espacios")
    void debeManejarSoloEspacios() {
        String soloEspacios = "   ";
        String resultado = EmailNormalizer.normalize(soloEspacios);
        assertThat(resultado).isEmpty();
    }
}
