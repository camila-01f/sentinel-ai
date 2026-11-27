package com.ujmd.sentinelai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SentinelViewModelTest {

    private class FakeStorage : TokenStorage {
        private var token: String? = null
        override fun saveSession(token: String, rol: String, nombre: String) { this.token = token }
        override fun getToken(): String? = token
        override fun getRol(): String = "AGENTE"
        override fun getNombre(): String = "Test"
        override fun getBaseUrl(): String = "http://localhost:8080/"
        override fun saveBaseUrl(url: String) {}
        override fun clear() { token = null }
    }

    @Test
    fun loginConCamposVacios_muestraErrorValidacion() {
        val vm = LoginViewModel({ throw IllegalStateException("No debe llamar red") }, FakeStorage())
        vm.iniciarSesion("", "") {}
        assertEquals("Ingrese correo y contraseña", vm.error)
    }

    @Test
    fun incidenteConDescripcionCorta_rechazaRegistro() {
        val vm = IncidentesViewModel { throw IllegalStateException("No debe llamar red") }
        vm.registrarIncidente("Fuego", "TEXTO", 13.6, -89.2, "", null) {}
        assertNotNull(vm.error)
        assertEquals("La descripción debe tener al menos 10 caracteres (HU-02)", vm.error)
    }

    @Test
    fun recursoConNombreVacio_rechazaGuardado() {
        val vm = RecursosViewModel { throw IllegalStateException("No debe llamar red") }
        vm.guardarRecurso(null, "   ", 1L, true) {}
        assertEquals("El nombre del recurso es obligatorio", vm.error)
    }
}