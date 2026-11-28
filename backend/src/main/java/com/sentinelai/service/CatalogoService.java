package com.sentinelai.service;

import com.sentinelai.dto.CatalogoDTO;
import com.sentinelai.repository.EstadoRepository;
import com.sentinelai.repository.PrioridadRepository;
import com.sentinelai.repository.TipoRecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final PrioridadRepository prioridades;
    private final EstadoRepository estados;
    private final TipoRecursoRepository tiposRecurso;

    public CatalogoService(PrioridadRepository prioridades, EstadoRepository estados, TipoRecursoRepository tiposRecurso) {
        this.prioridades = prioridades;
        this.estados = estados;
        this.tiposRecurso = tiposRecurso;
    }

    public List<CatalogoDTO> prioridades() {
        return prioridades.findAllByOrderByNivelAsc().stream().map(p -> new CatalogoDTO(p.getId(), p.getNombre())).toList();
    }

    public List<CatalogoDTO> estados() {
        return estados.findAllByOrderByOrdenAsc().stream().map(e -> new CatalogoDTO(e.getId(), e.getNombre())).toList();
    }

    public List<CatalogoDTO> tiposRecurso() {
        return tiposRecurso.findAllByOrderByIdAsc().stream().map(t -> new CatalogoDTO(t.getId(), t.getNombre())).toList();
    }
}
