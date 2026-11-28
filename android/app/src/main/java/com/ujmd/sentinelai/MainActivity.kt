package com.ujmd.sentinelai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SentinelAppNavigation()
                }
            }
        }
    }
}

@Composable
fun SentinelAppNavigation() {
    val context = LocalContext.current
    val storage = remember { SessionManager(context) }
    val apiProvider = remember { { RetrofitClient.create(storage) } }

    val loginVm = remember { LoginViewModel(apiProvider, storage) }
    val incidentesVm = remember { IncidentesViewModel(apiProvider) }
    val recursosVm = remember { RecursosViewModel(apiProvider) }

    val navController = rememberNavController()
    var incidenteSeleccionado by remember { mutableStateOf<Incidente?>(null) }

    val startDestination = if (!storage.getToken().isNullOrBlank()) "incidentes" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                viewModel = loginVm,
                storage = storage,
                onLoginSuccess = {
                    navController.navigate("incidentes") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("incidentes") {
            IncidentesListScreen(
                viewModel = incidentesVm,
                storage = storage,
                onNuevoIncidente = { navController.navigate("nuevo_incidente") },
                onVerDetalle = { inc ->
                    incidenteSeleccionado = inc
                    navController.navigate("detalle_incidente")
                },
                onIrRecursos = { navController.navigate("recursos") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("incidentes") { inclusive = true }
                    }
                }
            )
        }

        composable("nuevo_incidente") {
            NuevoIncidenteScreen(
                viewModel = incidentesVm,
                onBack = { navController.popBackStack() },
                onIrRevisionIa = { nuevoId ->
                    navController.navigate("revision_ia/$nuevoId") {
                        popUpTo("nuevo_incidente") { inclusive = true }
                    }
                }
            )
        }

        composable("detalle_incidente") {
            incidenteSeleccionado?.let { inc ->
                DetalleIncidenteScreen(
                    incidente = inc,
                    onBack = { navController.popBackStack() },
                    onIrRevisionIa = { id -> navController.navigate("revision_ia/$id") }
                )
            }
        }

        composable("recursos") {
            RecursosScreen(
                viewModel = recursosVm,
                onBack = { navController.popBackStack() }
            )
        }

        // Ruta lista para que el Integrante C conecte su pantalla de Revisión IA
        composable(
            route = "revision_ia/{incidenteId}",
            arguments = listOf(navArgument("incidenteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("incidenteId") ?: 0L
            RevisionIaBridgeScreen(
                incidenteId = id,
                onBack = {
                    navController.navigate("incidentes") {
                        popUpTo("incidentes") { inclusive = true }
                    }
                }
            )
        }
    }
}