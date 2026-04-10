package com.ethandev.cafecontable.data.local.dbmodel

data class LiquidacionPendienteDb(
    val asignacionId: String,
    val pedidoId: String,
    val mezclaId: String,
    val cliente: String?,
    val cantidadKg: Double,
    val precioBaseKg: Long,
    val factorReal: Double
)