package com.ethandev.cafecontable.domain.model

data class LiquidacionPendiente(
    val asignacionId: String,
    val pedidoId: String,
    val mezclaId: String,
    val cliente: String?,
    val cantidadKg: Double,
    val precioBaseKg: Long,
    val factorReal: Double
)