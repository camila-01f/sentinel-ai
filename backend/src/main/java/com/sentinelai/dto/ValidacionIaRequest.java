package com.sentinelai.dto;

import jakarta.validation.constraints.*;

public record ValidacionIaRequest(
    @NotNull Boolean aceptada,
    @NotNull @Min(1) @Max(6) Long categoriaId,
    @NotNull @Min(1) @Max(4) Long prioridadId
) {}