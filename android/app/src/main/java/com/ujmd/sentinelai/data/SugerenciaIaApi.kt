package com.ujmd.sentinelai.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SugerenciaIaApi {

    @POST("api/ia/sugerencias")
    suspend fun guardar(
        @Body request: SugerenciaIaRequest
    ): SugerenciaIaResponse

    @GET("api/ia/sugerencias/{id}")
    suspend fun consultar(
        @Path("id") id: Long
    ): SugerenciaIaResponse

    @POST("api/ia/sugerencias/{id}/validar")
    suspend fun validar(
        @Path("id") id: Long,
        @Body request: ValidacionIaRequest
    ): SugerenciaIaResponse
}