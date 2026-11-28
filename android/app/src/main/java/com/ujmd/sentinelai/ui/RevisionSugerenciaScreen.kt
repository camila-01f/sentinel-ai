package com.ujmd.sentinelai.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ujmd.sentinelai.data.SugerenciaIaResponse
import com.ujmd.sentinelai.data.SugerenciaIaApi
import com.ujmd.sentinelai.data.ValidacionIaRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

@Composable
fun RevisionSugerenciaScreen(
    sugerencia: SugerenciaIaResponse,
    onAceptar: () -> Unit,
    onCorregir: (Long, Long) -> Unit,
    guardando: Boolean = false
) {
    var categoriaId by remember(sugerencia.id) {
        mutableLongStateOf(sugerencia.categoria_sugerida_id)
    }

    var prioridadId by remember(sugerencia.id) {
        mutableLongStateOf(sugerencia.prioridad_sugerida_id)
    }

    val categorias = mapOf(
        1L to "Accidente de tránsito",
        2L to "Incendio",
        3L to "Inundación",
        4L to "Árbol o estructura caída",
        5L to "Emergencia médica",
        6L to "Otro"
    )

    val prioridades = mapOf(
        1L to "Crítica",
        2L to "Alta",
        3L to "Media",
        4L to "Baja"
    )

    val editable = sugerencia.aceptada == null && !guardando

    val modificada =
        categoriaId != sugerencia.categoria_sugerida_id ||
        prioridadId != sugerencia.prioridad_sugerida_id

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Sugerencia #${sugerencia.id}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Incidente #${sugerencia.incidente_id}")

                Text(
                    if (sugerencia.fuente == "IA")
                        "Fuente: Inteligencia artificial"
                    else
                        "Fuente: Reglas de respaldo"
                )

                HorizontalDivider()

                Text(
                    "Categoría sugerida: " +
                    (categorias[sugerencia.categoria_sugerida_id]
                        ?: "Desconocida")
                )

                Text(
                    "Prioridad sugerida: " +
                    (prioridades[sugerencia.prioridad_sugerida_id]
                        ?: "Desconocida")
                )

                Text(
                    "Justificación",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(sugerencia.justificacion)

                Text(
                    when (sugerencia.aceptada) {
                        true -> "Estado: Aceptada"
                        false -> "Estado: Rechazada y corregida"
                        null -> "Estado: Pendiente de revisión"
                    },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (sugerencia.aceptada == null) {
            Text(
                "Categoría definitiva",
                style = MaterialTheme.typography.titleMedium
            )

            categorias.forEach { (id, nombre) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = categoriaId == id,
                        onClick = { categoriaId = id },
                        enabled = editable
                    )
                    Text(nombre)
                }
            }

            Text(
                "Prioridad definitiva",
                style = MaterialTheme.typography.titleMedium
            )

            prioridades.forEach { (id, nombre) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = prioridadId == id,
                        onClick = { prioridadId = id },
                        enabled = editable
                    )
                    Text(nombre)
                }
            }

            Button(
                onClick = onAceptar,
                enabled = editable && !modificada,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aceptar sugerencia")
            }

            OutlinedButton(
                onClick = {
                    onCorregir(categoriaId, prioridadId)
                },
                enabled = editable && modificada,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar corrección")
            }
        }
    }
}

@Composable
fun RevisionSugerenciaRoute(
    api: SugerenciaIaApi,
    usuarioId: Long,
    onVolver: () -> Unit
) {
    var idTexto by remember { mutableStateOf("") }
    var sugerencia by remember {
        mutableStateOf<SugerenciaIaResponse?>(null)
    }

    var cargando by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmacion by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val ocupado = cargando || guardando

    fun mensajeError(e: Exception): String {
        return when ((e as? HttpException)?.code()) {
            400 -> "Datos inválidos. Revisa la clasificación."
            403 -> "Solo un supervisor puede validar."
            404 -> "No se encontró el recurso solicitado."
            409 -> "La sugerencia ya fue validada. Consulta nuevamente."
            else -> "No se pudo completar la operación. Verifica la conexión."
        }
    }

    fun consultar() {
        val id = idTexto.toLongOrNull()

        if (id == null || id <= 0) {
            error = "Introduce un ID válido."
            return
        }

        if (ocupado) return

        cargando = true
        error = null
        confirmacion = null
        sugerencia = null

        scope.launch {
            try {
                sugerencia = api.consultar(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = mensajeError(e)
            } finally {
                cargando = false
            }
        }
    }

    fun validar(
        aceptada: Boolean,
        categoriaId: Long,
        prioridadId: Long
    ) {
        val actual = sugerencia ?: return

        if (ocupado || actual.aceptada != null) return

        guardando = true
        error = null
        confirmacion = null

        scope.launch {
            try {
                sugerencia = api.validar(
                    actual.id,
                    ValidacionIaRequest(
                        aceptada = aceptada,
                        categoriaId = categoriaId,
                        prioridadId = prioridadId,
                        usuarioId = usuarioId
                    )
                )

                confirmacion = if (aceptada)
                    "Sugerencia aceptada correctamente."
                else
                    "Corrección guardada correctamente."

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = mensajeError(e)
            } finally {
                guardando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onVolver,
            enabled = !ocupado
        ) {
            Text("Volver")
        }

        Text(
            "Revisión de sugerencias IA",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = idTexto,
            onValueChange = {
                idTexto = it.filter { c -> c.isDigit() }
                sugerencia = null
                confirmacion = null
                error = null
            },
            label = { Text("ID de sugerencia") },
            singleLine = true,
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { consultar() },
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Consultar sugerencia")
        }

        if (ocupado) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                if (guardando)
                    "Guardando decisión..."
                else
                    "Consultando servidor..."
            )
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        confirmacion?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        sugerencia?.let { actual ->
            RevisionSugerenciaScreen(
                sugerencia = actual,
                guardando = guardando,
                onAceptar = {
                    validar(
                        true,
                        actual.categoria_sugerida_id,
                        actual.prioridad_sugerida_id
                    )
                },
                onCorregir = { categoria, prioridad ->
                    validar(false, categoria, prioridad)
                }
            )
        }
    }
}