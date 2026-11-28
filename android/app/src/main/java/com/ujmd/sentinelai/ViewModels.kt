package com.ujmd.sentinelai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.UUID

class LoginViewModel(
    private val apiProvider: () -> SentinelApi,
    private val storage: TokenStorage
) : ViewModel() {
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun iniciarSesion(correo: String, password: String, onSuccess: () -> Unit) {
        if (correo.isBlank() || password.isBlank()) {
            error = "Ingrese correo y contraseña"
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                val response = apiProvider().login(LoginRequest(correo.trim(), password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body.resolvedToken()
                    if (token.isNotBlank()) {
                        storage.saveSession(token, body.resolvedRol(), body.resolvedNombre())
                        onSuccess()
                    } else {
                        error = "El servidor no devolvió un token válido"
                    }
                } else {
                    error = if (response.code() == 401 || response.code() == 403) {
                        "Credenciales incorrectas"
                    } else {
                        "Error del servidor (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                error = "No se pudo conectar al servidor: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }
}

class IncidentesViewModel(
    private val apiProvider: () -> SentinelApi
) : ViewModel() {
    var incidentes by mutableStateOf<List<Incidente>>(emptyList())
    var categorias by mutableStateOf<List<CatalogoItem>>(emptyList())
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var mensajeExito by mutableStateOf<String?>(null)

    fun cargarTodo() {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                incidentes = apiProvider().getIncidentes()
                categorias = try { apiProvider().getCategorias() } catch (_: Exception) { emptyList() }
            } catch (e: Exception) {
                error = "Error al cargar incidentes: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun registrarIncidente(
        descripcion: String,
        origen: String,
        latitud: Double,
        longitud: Double,
        referencia: String,
        categoriaId: Long?,
        onCreated: (Long?) -> Unit
    ) {
        if (descripcion.trim().length < 10) {
            error = "La descripción debe tener al menos 10 caracteres (HU-02)"
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                val req = IncidenteRequest(
                    uuid = UUID.randomUUID().toString(),
                    descripcion = descripcion.trim(),
                    origen = origen,
                    latitud = latitud,
                    longitud = longitud,
                    referencia = referencia.trim(),
                    categoriaId = categoriaId
                )
                val res = apiProvider().crearIncidente(req)
                if (res.isSuccessful) {
                    mensajeExito = "Incidente registrado correctamente"
                    cargarTodo()
                    onCreated(res.body()?.id)
                } else {
                    error = "Error al guardar incidente (${res.code()})"
                }
            } catch (e: Exception) {
                error = "Fallo de red: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }
}

class RecursosViewModel(
    private val apiProvider: () -> SentinelApi
) : ViewModel() {
    var recursos by mutableStateOf<List<Recurso>>(emptyList())
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun cargarRecursos() {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                recursos = apiProvider().getRecursos()
            } catch (e: Exception) {
                error = "Error al cargar recursos: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun guardarRecurso(id: Long?, nombre: String, tipoId: Long, disponible: Boolean, onDone: () -> Unit) {
        if (nombre.isBlank()) {
            error = "El nombre del recurso es obligatorio"
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                val req = RecursoRequest(nombre.trim(), tipoId, disponible)
                val res = if (id == null) {
                    apiProvider().crearRecurso(req)
                } else {
                    apiProvider().actualizarRecurso(id, req)
                }
                if (res.isSuccessful) {
                    cargarRecursos()
                    onDone()
                } else if (res.code() == 403) {
                    error = "Acceso denegado (403): Solo el Supervisor puede gestionar recursos"
                } else {
                    error = "Error al guardar recurso (${res.code()})"
                }
            } catch (e: Exception) {
                error = "Error de red: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun eliminarRecurso(id: Long) {
        viewModelScope.launch {
            try {
                val res = apiProvider().eliminarRecurso(id)
                if (res.isSuccessful) {
                    cargarRecursos()
                } else if (res.code() == 403) {
                    error = "Acceso denegado (403): Solo el Supervisor puede eliminar"
                }
            } catch (e: Exception) {
                error = "Error al eliminar: ${e.localizedMessage}"
            }
        }
    }
}