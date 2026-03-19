package com.ethandev.cafecontable.data.local.entity

data class CompraHistorialRow(
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
)
