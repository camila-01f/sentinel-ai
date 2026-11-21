package com.sentinelai.controller;

import com.sentinelai.dto.CategoriaDTO;
import com.sentinelai.service.CategoriaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    /** GET /api/categorias -> catálogo de categorías de incidentes */
    @GetMapping
    public List<CategoriaDTO> listar() {
        return service.listar();
    }

    /** GET /api/categorias/{id} -> una categoría (404 si no existe) */
    @GetMapping("/{id}")
    public CategoriaDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }
}
