package com.sentinelai.service;

import com.sentinelai.dto.ClasificacionResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ClasificacionReglasServiceTest {

    private final ClasificacionReglasService servicio =
        new ClasificacionReglasService();

    @ParameterizedTest
    @CsvSource({
        "Una vivienda esta en llamas con una persona atrapada, 2, 1",
        "Se produjo un choque entre dos vehiculos, 1, 2",
        "Un accidente de transito dejo personas heridas, 1, 1",
        "Existe una inundacion en la comunidad, 3, 2",
        "Un poste electrico cayo y bloquea la calle, 4, 2",
        "Hay un arbol caido en un terreno, 4, 3",
        "Una persona esta inconsciente, 5, 1",
        "Se reporta una situacion desconocida, 6, 3",
        "INCENDIO EN UNA VIVIENDA, 2, 2",
        "Se reporta una inundaci\u00f3n en la zona, 3, 2"
    })
    void clasificaIncidentes(
        String descripcion,
        long categoriaEsperada,
        long prioridadEsperada
    ) {
        ClasificacionResponse resultado =
            servicio.clasificar(descripcion);

        assertEquals(categoriaEsperada,
            resultado.categoriaId().longValue());

        assertEquals(prioridadEsperada,
            resultado.prioridadId().longValue());

        assertEquals("REGLAS", resultado.fuente());

        assertNotNull(resultado.justificacion());
        assertFalse(resultado.justificacion().isBlank());
        assertTrue(resultado.justificacion().length() <= 500);
    }
}