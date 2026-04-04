package com.ethandev.cafecontable.data.local.entity

data class CompraDisponibleDb(
    val compraId: String,
    val productoId: String,
    val productoNombre: String,
    val cantidadDisponible: Double,
    val precioUnitCompra: Long
)