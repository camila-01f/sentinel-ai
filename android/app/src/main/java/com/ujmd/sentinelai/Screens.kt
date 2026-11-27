package com.ujmd.sentinelai

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import java.util.Locale

// 1. PANTALLA DE LOGIN + CONFIGURACIÓN URL SERVIDOR (HU-01)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    storage: TokenStorage,
    onLoginSuccess: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf(storage.getBaseUrl()) }
    var mostrarConfigUrl by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sentinel AI") },
                actions = {
                    IconButton(onClick = { mostrarConfigUrl = !mostrarConfigUrl }) {
                        Icon(Icons.Default.Settings, contentDescription = "URL Servidor")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gestión Inteligente de Incidentes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))

            if (mostrarConfigUrl) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = {
                        baseUrl = it
                        storage.saveBaseUrl(it)
                    },
                    label = { Text("URL Backend (ej. ngrok)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo institucional") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            viewModel.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = { viewModel.iniciarSesion(correo, password, onLoginSuccess) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.cargando
            ) {
                if (viewModel.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                } else {
                    Text("Iniciar Sesión")
                }
            }
        }
    }
}

// 2. LISTA DE INCIDENTES (HU-02 / Navegación principal)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentesListScreen(
    viewModel: IncidentesViewModel,
    storage: TokenStorage,
    onNuevoIncidente: () -> Unit,
    onVerDetalle: (Incidente) -> Unit,
    onIrRecursos: () -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.cargarTodo() }
    val esSupervisor = storage.getRol().uppercase().contains("SUPERVISOR")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Incidentes", style = MaterialTheme.typography.titleMedium)
                        Text("${storage.getNombre()} · ${storage.getRol()}", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    if (esSupervisor) {
                        TextButton(onClick = onIrRecursos) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Recursos")
                        }
                    }
                    IconButton(onClick = { viewModel.cargarTodo() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                    IconButton(onClick = {
                        storage.clear()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoIncidente) {
                Icon(Icons.Default.Add, contentDescription = "Reportar incidente")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when {
                viewModel.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                viewModel.error != null -> {
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(viewModel.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.cargarTodo() }) { Text("Reintentar") }
                    }
                }
                viewModel.incidentes.isEmpty() -> {
                    Text(
                        "No hay incidentes registrados.\nPulsa + para reportar el primero.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(viewModel.incidentes) { inc ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onVerDetalle(inc) },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = inc.displayCategoria(),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("${inc.displayPrioridad()} · ${inc.displayEstado()}") }
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(inc.descripcion, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                                    if (!inc.referencia.isNullOrBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("📍 ${inc.referencia}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. FORMULARIO NUEVO INCIDENTE CON GPS Y VOZ (HU-02 y HU-03)
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoIncidenteScreen(
    viewModel: IncidentesViewModel,
    onBack: () -> Unit,
    onIrRevisionIa: (Long) -> Unit
) {
    val context = LocalContext.current
    var descripcion by remember { mutableStateOf("") }
    var origen by remember { mutableStateOf("TEXTO") }
    var referencia by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf(13.6929) }
    var longitud by remember { mutableStateOf(-89.2182) }
    var gpsEstado by remember { mutableStateOf("Coordenadas base listas (pulsa GPS para actualizar)") }
    var categoriaSeleccionada by remember { mutableStateOf<CatalogoItem?>(null) }
    var errorVoz by remember { mutableStateOf<String?>(null) }

    // Lanzador de reconocimiento de voz (HU-03)
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val textoDictado = matches?.firstOrNull()
            if (!textoDictado.isNullOrBlank()) {
                descripcion = if (descripcion.isBlank()) textoDictado else "$descripcion $textoDictado"
                origen = "VOZ"
                errorVoz = null
            }
        }
    }

    // Lanzador de permisos de ubicación GPS (HU-02)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    latitud = loc.latitude
                    longitud = loc.longitude
                    gpsEstado = "Ubicación GPS obtenida del dispositivo"
                } else {
                    gpsEstado = "GPS activo (usando coordenadas predeterminadas del emulador)"
                }
            }
        } else {
            gpsEstado = "Permiso GPS denegado (usando coordenadas por defecto)"
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Incidente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del incidente (mín. 10 caracteres)") },
                modifier = Modifier.fillMaxWidth().height(130.dp)
            )

            // Botón de dictado por voz (HU-03)
            OutlinedButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("es", "SV"))
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Describa el incidente...")
                    }
                    try {
                        voiceLauncher.launch(intent)
                    } catch (_: Exception) {
                        errorVoz = "Reconocimiento de voz no disponible en este emulador; puede escribir manualmente."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Dictar descripción por voz ($origen)")
            }

            errorVoz?.let {
                Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }

            // Tarjeta de ubicación GPS
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("📍 Lat: $latitud, Lon: $longitud", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(gpsEstado, style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = {
                        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Actualizar GPS")
                    }
                }
            }

            OutlinedTextField(
                value = referencia,
                onValueChange = { referencia = it },
                label = { Text("Referencia de ubicación (ej. Frente a parque)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (viewModel.categorias.isNotEmpty()) {
                Text("Categoría inicial (opcional, IA sugerirá):", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    viewModel.categorias.take(3).forEach { cat ->
                        FilterChip(
                            selected = categoriaSeleccionada?.id == cat.id,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(cat.nombre, maxLines = 1) }
                        )
                    }
                }
            }

            viewModel.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.registrarIncidente(
                        descripcion = descripcion,
                        origen = origen,
                        latitud = latitud,
                        longitud = longitud,
                        referencia = referencia,
                        categoriaId = categoriaSeleccionada?.id
                    ) { nuevoId ->
                        if (nuevoId != null) onIrRevisionIa(nuevoId) else onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.cargando
            ) {
                Text(if (viewModel.cargando) "Guardando..." else "Guardar y Analizar con IA")
            }
        }
    }
}

// 4. DETALLE DE INCIDENTE (HU-02 + Enlace a Revisión IA del Integrante C)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleIncidenteScreen(
    incidente: Incidente,
    onBack: () -> Unit,
    onIrRevisionIa: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incidente #${incidente.id ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Categoría: ${incidente.displayCategoria()}", fontWeight = FontWeight.Bold)
                    Text("Prioridad: ${incidente.displayPrioridad()}")
                    Text("Estado actual: ${incidente.displayEstado()}")
                    Text("Origen de reporte: ${incidente.origen ?: "TEXTO"}")
                    HorizontalDivider()
                    Text("Descripción:", fontWeight = FontWeight.Bold)
                    Text(incidente.descripcion)
                    HorizontalDivider()
                    Text("Ubicación GPS: ${incidente.latitud ?: 0.0}, ${incidente.longitud ?: 0.0}")
                    if (!incidente.referencia.isNullOrBlank()) {
                        Text("Referencia: ${incidente.referencia}")
                    }
                }
            }

            incidente.id?.let { id ->
                Button(
                    onClick = { onIrRevisionIa(id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Revisar Sugerencia de IA")
                }
            }
        }
    }
}

// 5. GESTIÓN DE RECURSOS — CRUD (HU-06)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecursosScreen(
    viewModel: RecursosViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.cargarRecursos() }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var recursoEditar by remember { mutableStateOf<Recurso?>(null) }

    val tiposCatalogo = listOf(
        CatalogoItem(1, "Ambulancia"),
        CatalogoItem(2, "Unidad de bomberos"),
        CatalogoItem(3, "Cuadrilla municipal"),
        CatalogoItem(4, "Patrulla")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Recursos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                recursoEditar = null
                mostrarDialogo = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo recurso")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            viewModel.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            if (viewModel.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.recursos) { rec ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rec.nombre, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${rec.displayTipo()} · ${if (rec.disponible) "Disponible" else "Ocupado"}",
                                        color = if (rec.disponible) Color(0xFF15803D) else Color(0xFFB91C1C),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = {
                                    recursoEditar = rec
                                    mostrarDialogo = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                rec.id?.let { id ->
                                    IconButton(onClick = { viewModel.eliminarRecurso(id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        var nombre by remember { mutableStateOf(recursoEditar?.nombre ?: "") }
        var tipoId by remember { mutableStateOf(recursoEditar?.tipoRecursoId ?: recursoEditar?.tipoRecurso?.id ?: 1L) }
        var disponible by remember { mutableStateOf(recursoEditar?.disponible ?: true) }

        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(if (recursoEditar == null) "Nuevo Recurso" else "Editar Recurso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre (ej. Ambulancia A-01)") },
                        singleLine = true
                    )
                    Text("Tipo de recurso:", style = MaterialTheme.typography.labelMedium)
                    tiposCatalogo.forEach { tipo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { tipoId = tipo.id }
                        ) {
                            RadioButton(selected = tipoId == tipo.id, onClick = { tipoId = tipo.id })
                            Text(tipo.nombre)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = disponible, onCheckedChange = { disponible = it })
                        Spacer(Modifier.width(8.dp))
                        Text(if (disponible) "Disponible" else "Ocupado")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.guardarRecurso(recursoEditar?.id, nombre, tipoId, disponible) {
                        mostrarDialogo = false
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}

// 6. PUENTE DE NAVEGACIÓN PARA EL INTEGRANTE C (Actividad 11)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionIaBridgeScreen(incidenteId: Long, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisión Sugerencia IA (#$incidenteId)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Espacio conectado para módulo de IA (Integrante C)", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Incidente ID recibido: $incidenteId")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Volver a Incidentes") }
        }
    }
}