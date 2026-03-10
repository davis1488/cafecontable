package com.ethandev.cafecontable.domain.model

data class CompraHistorialItem(
    val id: String,
    val fecha: Long,
    val productoNombre: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitCompra: Long,
    val proveedor: String?,
    val esCredito: Boolean,
    val nota: String?
) {
    val total: Long
        get() = (cantidad * precioUnitCompra).toLong()
}