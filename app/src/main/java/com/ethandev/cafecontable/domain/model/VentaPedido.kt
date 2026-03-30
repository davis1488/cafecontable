package com.ethandev.cafecontable.domain.model

data class VentaPedido(
    val id: String,
    val fecha: Long,
    val cliente: String,
    val cantidadPactada: Double,
    val unidad: String,
    val precioUnitVenta: Long,
    val totalVenta: Long,
    val cantidadEntregada: Double,
    val estado: String,
    val nota: String?
)