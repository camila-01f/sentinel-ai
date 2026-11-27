package com.ujmd.sentinelai

import com.google.gson.annotations.SerializedName

// --- AUTENTICACIÓN ---
data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val token: String? = null,
    val accessToken: String? = null,
    val rol: String? = null,
    val nombre: String? = null,
    val usuario: UsuarioDto? = null
) {
    fun resolvedToken(): String = token ?: accessToken ?: ""
    fun resolvedRol(): String = rol ?: usuario?.rol ?: "AGENTE"
    fun resolvedNombre(): String = nombre ?: usuario?.nombre ?: "Usuario"
}

data class UsuarioDto(
    val id: Long? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val rol: String? = null
)

// --- CATÁLOGOS ---
data class CatalogoItem(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null
)

// --- INCIDENTES ---
data class Incidente(
    val id: Long? = null,
    val uuid: String? = null,
    val descripcion: String = "",
    val origen: String? = "TEXTO",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val referencia: String? = null,
    @SerializedName("categoriaId") val categoriaId: Long? = null,
    @SerializedName("categoriaNombre") val categoriaNombre: String? = null,
    val categoria: CatalogoItem? = null,
    @SerializedName("prioridadNombre") val prioridadNombre: String? = null,
    val prioridad: CatalogoItem? = null,
    @SerializedName("estadoNombre") val estadoNombre: String? = null,
    val estado: CatalogoItem? = null,
    val fechaReporte: String? = null
) {
    fun displayCategoria(): String = categoria?.nombre ?: categoriaNombre ?: "Sin clasificar"
    fun displayPrioridad(): String = prioridad?.nombre ?: prioridadNombre ?: "Media"
    fun displayEstado(): String = estado?.nombre ?: estadoNombre ?: "Reportado"
}

data class IncidenteRequest(
    val uuid: String,
    val descripcion: String,
    val origen: String,
    val latitud: Double,
    val longitud: Double,
    val referencia: String,
    val categoriaId: Long? = null
)

// --- RECURSOS ---
data class Recurso(
    val id: Long? = null,
    val nombre: String = "",
    @SerializedName("tipoRecursoId") val tipoRecursoId: Long? = null,
    @SerializedName("tipoNombre") val tipoNombre: String? = null,
    val tipoRecurso: CatalogoItem? = null,
    val disponible: Boolean = true
) {
    fun displayTipo(): String = tipoRecurso?.nombre ?: tipoNombre ?: "Unidad Municipal"
}

data class RecursoRequest(
    val nombre: String,
    val tipoRecursoId: Long,
    val disponible: Boolean
)