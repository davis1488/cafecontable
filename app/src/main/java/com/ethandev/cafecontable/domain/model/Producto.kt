package com.ethandev.cafecontable.domain.model

data class Producto(
    val id: String,
    val nombre: String,
    val unidad: String,
    val precioVenta: Long
)