package com.sentinelai.controller;

import com.sentinelai.dto.ClasificacionRequest;
import com.sentinelai.dto.ClasificacionResponse;
import com.sentinelai.service.ClasificacionIaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ia")
public class ClasificacionIaController {

    private final ClasificacionIaService servicio;

    public ClasificacionIaController(ClasificacionIaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/clasificar")
    public ClasificacionResponse clasificar(
            @Valid @RequestBody ClasificacionRequest request) {
        return servicio.clasificar(request.descripcion());
    }
}
