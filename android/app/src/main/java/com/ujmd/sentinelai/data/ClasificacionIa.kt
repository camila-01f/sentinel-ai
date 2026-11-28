package com.ujmd.sentinelai.data

data class ClasificacionRequest(
    val descripcion: String
)

data class ClasificacionResponse(
    val categoriaId: Long,
    val categoria: String,
    val prioridadId: Long,
    val prioridad: String,
    val justificacion: String,
    val fuente: String
)