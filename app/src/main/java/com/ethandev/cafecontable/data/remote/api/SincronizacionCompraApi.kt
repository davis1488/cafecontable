
package com.ethandev.cafecontable.data.remote.api

import com.ethandev.cafecontable.data.remote.compra.SincronizarCompraRequest
import com.ethandev.cafecontable.data.remote.compra.SincronizarCompraResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SincronizacionCompraApi {

    @POST("api/compras/sincronizar")
    suspend fun sincronizarCompra(
        @Body request: SincronizarCompraRequest
    ): Response<SincronizarCompraResponse>
}
