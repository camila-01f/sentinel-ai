package com.sentinelai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Campos editables de un incidente. El estado se cambia con su propio flujo (Entrega 3). */
public record IncidenteActualizacionRequest(
        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
        String descripcion,

        @Size(max = 200, message = "La referencia admite máximo 200 caracteres")
        String referencia,

        Integer categoriaId,
        Integer prioridadId
) { }
