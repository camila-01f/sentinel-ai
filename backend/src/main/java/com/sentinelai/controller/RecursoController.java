package com.sentinelai.controller;

import com.sentinelai.dto.RecursoDTO;
import com.sentinelai.dto.RecursoRequest;
import com.sentinelai.service.RecursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD de recursos. Consultar: ambos roles. Crear, editar y eliminar: solo SUPERVISOR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/recursos")
public class RecursoController {

    private final RecursoService service;

    public RecursoController(RecursoService service) {
        this.service = service;
    }

    /** GET /api/recursos?disponible=true -> filtro opcional por disponibilidad */
    @GetMapping
    public List<RecursoDTO> listar(@RequestParam(required = false) Boolean disponible) {
        return service.listar(disponible);
    }

    @GetMapping("/{id}")
    public RecursoDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<RecursoDTO> crear(@Valid @RequestBody RecursoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public RecursoDTO actualizar(@PathVariable Integer id, @Valid @RequestBody RecursoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
