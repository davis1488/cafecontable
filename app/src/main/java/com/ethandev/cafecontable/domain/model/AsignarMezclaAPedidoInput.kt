package com.ethandev.cafecontable.domain.model

data class AsignarMezclaAPedidoInput(
    val pedidoId: String,
    val mezclaId: String,
    val fecha: Long,
    val cantidadAsignada: Double,
    val precioUnitVenta: Long,
    val factor: Double,
    val nota: String?
)