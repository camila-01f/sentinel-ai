package com.sentinelai.controller;

import com.sentinelai.dto.SugerenciaIaRequest;
import com.sentinelai.dto.ValidacionIaRequest;
import com.sentinelai.service.SugerenciaIaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ia/sugerencias")
public class SugerenciaIaController {

    private final SugerenciaIaService servicio;

    public SugerenciaIaController(
        SugerenciaIaService servicio
    ) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> guardar(
        @Valid @RequestBody SugerenciaIaRequest request
    ) {
        return servicio.guardar(request);
    }

    @GetMapping("/{id}")
    public Map<String, Object> consultar(
        @PathVariable Long id
    ) {
        return servicio.consultar(id);
    }

    @PostMapping("/{id}/validar")
    public Map<String, Object> validar(
        @PathVariable Long id,
        @Valid @RequestBody ValidacionIaRequest request
    ) {
        return servicio.validar(id, request);
    }
}