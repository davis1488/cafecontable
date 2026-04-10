package com.ethandev.cafecontable.domain.model

data class LiquidacionHistorial(
    val id: String,
    val fecha: Long,
    val asignacionId: String,
    val pedidoId: String,
    val mezclaId: String,
    val cliente: String?,
    val cantidadKg: Double,
    val precioBaseKg: Long,
    val factorReal: Double,
    val valorBase: Long,
    val ajusteFactor: Long,
    val descuentoCooperativa: Long,
    val otrosDescuentos: Long,
    val valorNeto: Long,
    val nota: String?
)