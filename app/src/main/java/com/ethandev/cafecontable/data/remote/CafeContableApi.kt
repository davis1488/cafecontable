
package com.ethandev.cafecontable.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface CafeContableApi {

    @GET("api/compras")
    suspend fun obtenerCompras(): Response<List<CompraApiDto>>
}
