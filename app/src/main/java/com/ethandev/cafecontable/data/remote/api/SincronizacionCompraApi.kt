
package com.ethandev.cafecontable.data.remote.api

import com.ethandev.cafecontable.data.remote.dto.SincronizarCompraRequest
import com.ethandev.cafecontable.data.remote.dto.SincronizarCompraResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SincronizacionCompraApi {

    @POST("api/compras/sincronizar")
    suspend fun sincronizarCompra(
        @Body request: SincronizarCompraRequest
    ): Response<SincronizarCompraResponse>
}
