package com.ujmd.sentinelai

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SentinelApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/categorias")
    suspend fun getCategorias(): List<CatalogoItem>

    @GET("api/incidentes")
    suspend fun getIncidentes(): List<Incidente>

    @POST("api/incidentes")
    suspend fun crearIncidente(@Body request: IncidenteRequest): Response<Incidente>

    @GET("api/recursos")
    suspend fun getRecursos(): List<Recurso>

    @POST("api/recursos")
    suspend fun crearRecurso(@Body request: RecursoRequest): Response<Recurso>

    @PUT("api/recursos/{id}")
    suspend fun actualizarRecurso(@Path("id") id: Long, @Body request: RecursoRequest): Response<Recurso>

    @DELETE("api/recursos/{id}")
    suspend fun eliminarRecurso(@Path("id") id: Long): Response<Unit>
}

object RetrofitClient {
    fun create(storage: TokenStorage): SentinelApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                val token = storage.getToken()
                if (!token.isNullOrBlank()) {
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .build()

        val baseUrl = try {
            val saved = storage.getBaseUrl()
            if (saved.startsWith("http")) saved else "http://10.0.2.2:8080/"
        } catch (_: Exception) {
            "http://10.0.2.2:8080/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SentinelApi::class.java)
    }
}