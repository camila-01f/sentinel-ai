package com.sentinelai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecursoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite máximo 80 caracteres")
        String nombre,

        @NotNull(message = "El tipo de recurso es obligatorio")
        @Positive(message = "El tipo de recurso no es válido")
        Integer tipoRecursoId,

        /** Opcional: si no se envía, el recurso queda disponible. */
        Boolean disponible
) { }
