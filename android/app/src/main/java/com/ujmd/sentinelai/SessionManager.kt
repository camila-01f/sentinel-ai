package com.ujmd.sentinelai

import android.content.Context

interface TokenStorage {
    fun saveSession(token: String, rol: String, nombre: String)
    fun getToken(): String?
    fun getRol(): String
    fun getNombre(): String
    fun getBaseUrl(): String
    fun saveBaseUrl(url: String)
    fun clear()
}

class SessionManager(context: Context) : TokenStorage {
    private val prefs = context.getSharedPreferences("sentinel_prefs", Context.MODE_PRIVATE)

    override fun saveSession(token: String, rol: String, nombre: String) {
        prefs.edit()
            .putString("jwt_token", token)
            .putString("user_rol", rol)
            .putString("user_nombre", nombre)
            .apply()
    }

    override fun getToken(): String? = prefs.getString("jwt_token", null)

    override fun getRol(): String = prefs.getString("user_rol", "AGENTE") ?: "AGENTE"

    override fun getNombre(): String = prefs.getString("user_nombre", "Usuario") ?: "Usuario"

    override fun getBaseUrl(): String =
        prefs.getString("base_url", "http://10.0.2.2:8080/") ?: "http://10.0.2.2:8080/"

    override fun saveBaseUrl(url: String) {
        val limpia = if (url.endsWith("/")) url else "$url/"
        prefs.edit().putString("base_url", limpia).apply()
    }

    override fun clear() {
        val urlActual = getBaseUrl()
        prefs.edit().clear().putString("base_url", urlActual).apply()
    }
}
