package com.sentinelai.controller;

import com.sentinelai.dto.CatalogoDTO;
import com.sentinelai.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogos de solo lectura que la app usa para llenar listas desplegables. */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    @GetMapping("/prioridades")
    public List<CatalogoDTO> prioridades() {
        return service.prioridades();
    }

    @GetMapping("/estados")
    public List<CatalogoDTO> estados() {
        return service.estados();
    }

    @GetMapping("/tipos-recurso")
    public List<CatalogoDTO> tiposRecurso() {
        return service.tiposRecurso();
    }
}
