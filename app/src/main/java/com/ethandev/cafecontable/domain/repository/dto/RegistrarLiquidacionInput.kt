
package com.ethandev.cafecontable.domain.repository.dto

data class RegistrarLiquidacionInput(
    val asignacionId: String,
    val pedidoId: String,
    val mezclaId: String,
    val cliente: String?,
    val cantidadKg: Double,
    val precioBaseKg: Long,
    val factorReal: Double,
    val descuentoCooperativa: Long,
    val otrosDescuentos: Long,
    val nota: String?
)