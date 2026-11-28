package com.sentinelai.dto;

public record ClasificacionResponse(
    Long categoriaId,
    String categoria,
    Long prioridadId,
    String prioridad,
    String justificacion,
    String fuente
) {}
