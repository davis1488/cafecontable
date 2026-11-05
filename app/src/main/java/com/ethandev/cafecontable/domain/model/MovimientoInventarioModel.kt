package com.ethandev.cafecontable.domain.model

data class MovimientoInventarioModel(
    val id: String,
    val fecha: Long,
    val productoId: String,
    val tipo: String,
    val cantidad: Double,
    val costoUnit: Long,
    val docTipo: String?,
    val docId: String?,
    val nota: String?
)
