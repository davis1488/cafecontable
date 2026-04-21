package com.ethandev.cafecontable.domain.repository.dto

data class VentaPedidoInput(
    val cliente: String,
    val cantidadPactada: Double,
    val unidad: String,
    val precioUnitVenta: Long,
    val nota: String?
)