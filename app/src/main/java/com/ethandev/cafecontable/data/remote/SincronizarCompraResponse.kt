
package com.ethandev.cafecontable.data.remote.dto

data class SincronizarCompraResponse(
    val operacionSyncId: String,
    val compraId: Int,
    val yaSincronizada: Boolean,
    val estado: String
)
