package com.ethandev.cafecontable.domain.model

data class PreparacionEntrega(
    val id: String,
    val ventaId: String,
    val fecha: Long,
    val cantidadPreparada: Double,
    val costoTotal: Long,
    val costoPromedio: Long,
    val estado: String,
    val nota: String?
)

