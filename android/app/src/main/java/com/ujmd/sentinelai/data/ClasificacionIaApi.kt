package com.ujmd.sentinelai.data

import retrofit2.http.Body
import retrofit2.http.POST

interface ClasificacionIaApi {

    @POST("api/ia/clasificar")
    suspend fun clasificar(
        @Body request: ClasificacionRequest
    ): ClasificacionResponse
}