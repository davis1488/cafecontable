package com.ethandev.cafecontable.domain.model

data class MezclaDetalleInput(
    val compraId: String,
    val productoId: String,
    val productoNombre: String,
    val cantidadUsada: Double,
    val costoUnitCompra: Long
)