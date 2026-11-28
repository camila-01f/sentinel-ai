package com.sentinelai.controller;

import com.sentinelai.dto.IncidenteActualizacionRequest;
import com.sentinelai.dto.IncidenteDTO;
import com.sentinelai.dto.IncidenteRequest;
import com.sentinelai.security.UsuarioAutenticado;
import com.sentinelai.service.IncidenteService;
import com.sentinelai.service.ResultadoRegistro;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD de incidentes. Consultar, registrar y editar: ambos roles. Eliminar: solo SUPERVISOR. */
@RestController
@RequestMapping("/api/incidentes")
public class IncidenteController {

    private final IncidenteService service;

    public IncidenteController(IncidenteService service) {
        this.service = service;
    }

    @GetMapping
    public List<IncidenteDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public IncidenteDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    /** 201 si se creó; 200 si el uuid ya existía (reenvío sin duplicar). */
    @PostMapping
    public ResponseEntity<IncidenteDTO> registrar(@Valid @RequestBody IncidenteRequest request,
                                                  @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ResultadoRegistro resultado = service.registrar(request, usuario);
        HttpStatus status = resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.incidente());
    }

    @PutMapping("/{id}")
    public IncidenteDTO actualizar(@PathVariable Integer id, @Valid @RequestBody IncidenteActualizacionRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
