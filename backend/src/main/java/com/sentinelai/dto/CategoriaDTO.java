package com.sentinelai.dto;

import com.sentinelai.model.Categoria;

/** Lo que la API devuelve: no se expone la entidad JPA directamente. */
public record CategoriaDTO(Integer id, String nombre, String descripcion) {
    public static CategoriaDTO from(Categoria c) {
        return new CategoriaDTO(c.getId(), c.getNombre(), c.getDescripcion());
    }
}
