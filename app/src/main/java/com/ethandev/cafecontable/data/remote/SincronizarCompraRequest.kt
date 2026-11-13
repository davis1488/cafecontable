
package com.ethandev.cafecontable.data.remote.dto

data class SincronizarCompraRequest(
    val operacionSyncId: String,
    val compraLocalId: String,

    val productoNombre: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitCompra: Long,

    val proveedor: String?,
    val esCredito: Boolean,
    val nota: String?,
    val abono: Long,

    val operacionCompraId: String,
    val operacionNombre: String?,
    val operacionTipo: String?,
    val operacionDescripcion: String?,
    val operacionFecha: Long?,
    val operacionEstado: String?
)
