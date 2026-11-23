
package com.ethandev.cafecontable.data.remote.compra

data class SincronizarCompraResponse(
    val operacionSyncId: String,
    val compraId: Int,
    val yaSincronizada: Boolean,
    val estado: String
)
