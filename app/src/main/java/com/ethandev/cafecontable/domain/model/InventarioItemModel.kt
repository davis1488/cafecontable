package com.ethandev.cafecontable.domain.model

data class InventarioItemModel(
    val productoId: String,
    val nombre: String,
    val unidad: String,
    val existencia: Double
)
