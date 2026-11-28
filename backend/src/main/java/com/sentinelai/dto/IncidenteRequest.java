package com.sentinelai.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** Datos para registrar un incidente. El uuid lo genera la app para evitar duplicados al sincronizar. */
public record IncidenteRequest(
        @NotNull(message = "El uuid es obligatorio")
        UUID uuid,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
        String descripcion,

        @NotNull(message = "El origen es obligatorio")
        @Pattern(regexp = "(?i)TEXTO|VOZ", message = "El origen debe ser TEXTO o VOZ")
        String origen,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90", message = "Latitud fuera de rango")
        @DecimalMax(value = "90", message = "Latitud fuera de rango")
        Double latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180", message = "Longitud fuera de rango")
        @DecimalMax(value = "180", message = "Longitud fuera de rango")
        Double longitud,

        @Size(max = 200, message = "La referencia admite máximo 200 caracteres")
        String referencia,

        Integer categoriaId,
        Integer prioridadId,

        /** Hora real de captura (útil para registros offline). Si no se envía, se usa la hora del servidor. */
        LocalDateTime fechaReporte
) { }
