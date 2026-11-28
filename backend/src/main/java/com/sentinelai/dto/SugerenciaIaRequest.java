package com.sentinelai.dto;

import jakarta.validation.constraints.*;

public record SugerenciaIaRequest(
    @NotNull @Positive Long incidenteId,
    @NotNull @Min(1) @Max(6) Long categoriaId,
    @NotNull @Min(1) @Max(4) Long prioridadId,
    @NotBlank @Size(max = 500) String justificacion,
    @NotNull @Pattern(regexp = "IA|REGLAS") String fuente
) {}