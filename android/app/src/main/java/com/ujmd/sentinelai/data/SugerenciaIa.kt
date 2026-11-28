package com.ujmd.sentinelai.data

data class SugerenciaIaRequest(
    val incidenteId: Long,
    val categoriaId: Long,
    val prioridadId: Long,
    val justificacion: String,
    val fuente: String
)

data class ValidacionIaRequest(
    val aceptada: Boolean,
    val categoriaId: Long,
    val prioridadId: Long,
    val usuarioId: Long
)

data class SugerenciaIaResponse(
    val id: Long,
    val incidente_id: Long,
    val categoria_sugerida_id: Long,
    val prioridad_sugerida_id: Long,
    val justificacion: String,
    val fuente: String,
    val aceptada: Boolean?
)