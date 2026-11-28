package com.sentinelai.service;

import com.sentinelai.dto.RecursoDTO;
import com.sentinelai.dto.RecursoRequest;
import com.sentinelai.exception.ConflictoException;
import com.sentinelai.exception.RecursoNoEncontradoException;
import com.sentinelai.exception.SolicitudInvalidaException;
import com.sentinelai.model.Recurso;
import com.sentinelai.model.TipoRecurso;
import com.sentinelai.repository.RecursoRepository;
import com.sentinelai.repository.TipoRecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RecursoService {

    private final RecursoRepository recursos;
    private final TipoRecursoRepository tiposRecurso;

    public RecursoService(RecursoRepository recursos, TipoRecursoRepository tiposRecurso) {
        this.recursos = recursos;
        this.tiposRecurso = tiposRecurso;
    }

    @Transactional(readOnly = true)
    public List<RecursoDTO> listar(Boolean disponible) {
        List<Recurso> lista = disponible == null
                ? recursos.findAllByOrderByIdAsc()
                : recursos.findByDisponibleOrderByIdAsc(disponible);
        return lista.stream().map(RecursoDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public RecursoDTO obtener(Integer id) {
        return RecursoDTO.from(buscar(id));
    }

    public RecursoDTO crear(RecursoRequest request) {
        String nombre = request.nombre().trim();
        if (recursos.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe un recurso con el nombre " + nombre);
        }
        Recurso recurso = new Recurso();
        recurso.setNombre(nombre);
        recurso.setTipoRecurso(buscarTipo(request.tipoRecursoId()));
        recurso.setDisponible(request.disponible() == null || request.disponible());
        return RecursoDTO.from(recursos.save(recurso));
    }

    public RecursoDTO actualizar(Integer id, RecursoRequest request) {
        Recurso recurso = buscar(id);
        String nombre = request.nombre().trim();
        if (recursos.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe otro recurso con el nombre " + nombre);
        }
        recurso.setNombre(nombre);
        recurso.setTipoRecurso(buscarTipo(request.tipoRecursoId()));
        if (request.disponible() != null) {
            recurso.setDisponible(request.disponible());
        }
        return RecursoDTO.from(recurso);
    }

    public void eliminar(Integer id) {
        Recurso recurso = buscar(id);
        if (recursos.contarAsignaciones(id) > 0) {
            throw new ConflictoException(
                    "El recurso tiene asignaciones registradas; márcalo como no disponible en lugar de eliminarlo");
        }
        recursos.delete(recurso);
    }

    private Recurso buscar(Integer id) {
        return recursos.findConTipoById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el recurso con id " + id));
    }

    private TipoRecurso buscarTipo(Integer tipoId) {
        return tiposRecurso.findById(tipoId)
                .orElseThrow(() -> new SolicitudInvalidaException("No existe el tipo de recurso con id " + tipoId));
    }
}
