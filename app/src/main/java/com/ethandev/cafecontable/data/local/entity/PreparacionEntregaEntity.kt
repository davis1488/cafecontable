package com.ethandev.cafecontable.data.local.entity

data class PreparacionEntregaEntity(
    val id: String,
    val ventaId: String,
    val fecha: Long,
    val cantidadPreparada: Double,
    val costoPromedio: Long,
    val estado: String,
    val nota: String?
)
