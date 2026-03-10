package com.ethandev.cafecontable.data.local.entity

data class VentaHistorialRow(
    val id: String,
    val fecha: Long,
    val productoNombre: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitVenta: Long,
    val cliente: String?,
    val esCredito: Boolean,
    val nota: String?,
    val factor: Double

    )
//    {
//        val precioFinal: Double get() = precioUnitVenta * factor
//}