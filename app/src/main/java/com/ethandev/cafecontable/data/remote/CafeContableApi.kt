
package com.ethandev.cafecontable.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import com.ethandev.cafecontable.data.remote.dto.SincronizarCompraRequest
import com.ethandev.cafecontable.data.remote.dto.SincronizarCompraResponse

interface CafeContableApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>

    @GET("api/compras")
    suspend fun obtenerCompras(): Response<List<CompraApiDto>>

    @GET("api/compras")
    suspend fun obtenerComprasAutenticado(
        @Header("Authorization") token: String
    ): Response<List<CompraApiDto>>

    // NUEVO: Sincronización de compras offline
    @POST("api/compras/sincronizar")
    suspend fun sincronizarCompra(
        @Body request: SincronizarCompraRequest
    ): Response<SincronizarCompraResponse>
}
