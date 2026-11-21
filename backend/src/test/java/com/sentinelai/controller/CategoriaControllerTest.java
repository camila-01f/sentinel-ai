package com.sentinelai.controller;

import com.sentinelai.dto.CategoriaDTO;
import com.sentinelai.exception.RecursoNoEncontradoException;
import com.sentinelai.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean CategoriaService service;

    @Test
    void listarDevuelveCategorias() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new CategoriaDTO(1, "Accidente de tránsito", "Choques"),
                new CategoriaDTO(2, "Incendio", "Incendios")));

        mvc.perform(get("/api/categorias"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(2))
           .andExpect(jsonPath("$[0].nombre").value("Accidente de tránsito"));
    }

    @Test
    void categoriaInexistenteDevuelve404() throws Exception {
        when(service.obtener(99)).thenThrow(new RecursoNoEncontradoException("No existe la categoría con id 99"));

        mvc.perform(get("/api/categorias/99"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.mensaje").value("No existe la categoría con id 99"));
    }
}
