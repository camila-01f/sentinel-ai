package com.sentinelai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClasificacionRequest(
    @NotBlank
    @Size(min = 10, max = 3000)
    String descripcion
) {}
