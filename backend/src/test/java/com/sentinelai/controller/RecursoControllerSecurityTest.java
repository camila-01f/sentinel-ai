package com.sentinelai.controller;

import com.sentinelai.config.SecurityConfig;
import com.sentinelai.dto.RecursoDTO;
import com.sentinelai.security.JwtService;
import com.sentinelai.security.UsuarioAutenticado;
import com.sentinelai.service.RecursoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Comprueba las reglas de acceso por rol con el filtro JWT real. */
@WebMvcTest(RecursoController.class)
@Import(SecurityConfig.class)
class RecursoControllerSecurityTest {

    private static final String NUEVO = """
            {"nombre": "Ambulancia A-03", "tipoRecursoId": 1, "disponible": true}
            """;

    @Autowired MockMvc mvc;
    @MockitoBean RecursoService service;
    @MockitoBean JwtService jwtService;

    @BeforeEach
    void tokens() {
        when(jwtService.validar("token-supervisor")).thenReturn(Optional.of(
                new UsuarioAutenticado(1, "supervisor@sentinel.test", "Supervisor Demo", "SUPERVISOR")));
        when(jwtService.validar("token-agente")).thenReturn(Optional.of(
                new UsuarioAutenticado(2, "agente@sentinel.test", "Agente Demo", "AGENTE")));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mvc.perform(get("/api/recursos"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.estado").value(401));
    }

    @Test
    void agentePuedeConsultarRecursos() throws Exception {
        when(service.listar(null)).thenReturn(List.of(new RecursoDTO(1, "Ambulancia A-01", 1, "Ambulancia", true)));

        mvc.perform(get("/api/recursos").header("Authorization", "Bearer token-agente"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].nombre").value("Ambulancia A-01"));
    }

    @Test
    void agenteNoPuedeCrearRecursos() throws Exception {
        mvc.perform(post("/api/recursos").header("Authorization", "Bearer token-agente")
                    .contentType(MediaType.APPLICATION_JSON).content(NUEVO))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.estado").value(403));
        verify(service, never()).crear(any());
    }

    @Test
    void supervisorCreaRecursos() throws Exception {
        when(service.crear(any())).thenReturn(new RecursoDTO(7, "Ambulancia A-03", 1, "Ambulancia", true));

        mvc.perform(post("/api/recursos").header("Authorization", "Bearer token-supervisor")
                    .contentType(MediaType.APPLICATION_JSON).content(NUEVO))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void datosInvalidosResponden400() throws Exception {
        mvc.perform(post("/api/recursos").header("Authorization", "Bearer token-supervisor")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos.nombre").exists())
           .andExpect(jsonPath("$.campos.tipoRecursoId").exists());
    }
}
