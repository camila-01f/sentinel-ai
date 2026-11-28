package com.sentinelai.dto;

import com.sentinelai.model.Recurso;

public record RecursoDTO(Integer id, String nombre, Integer tipoRecursoId, String tipoNombre, boolean disponible) {

    public static RecursoDTO from(Recurso r) {
        return new RecursoDTO(r.getId(), r.getNombre(),
                r.getTipoRecurso().getId(), r.getTipoRecurso().getNombre(), r.isDisponible());
    }
}
