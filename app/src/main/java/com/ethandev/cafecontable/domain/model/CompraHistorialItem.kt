package com.ethandev.cafecontable.domain.model

data class CompraHistorialItem(
    val id: Int,
    val fecha: Long,
    val productoNombre: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitCompra: Long,
    val proveedor: String?,
    val esCredito: Boolean,
    val nota: String?,
    val estado: String
) {
    val total: Long
        get() = (cantidad * precioUnitCompra).toLong()
}