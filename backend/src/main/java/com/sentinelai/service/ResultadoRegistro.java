package com.sentinelai.service;

import com.sentinelai.dto.IncidenteDTO;

/** Resultado de registrar un incidente: creado = false si el uuid ya existía (reenvío desde la app). */
public record ResultadoRegistro(IncidenteDTO incidente, boolean creado) { }
