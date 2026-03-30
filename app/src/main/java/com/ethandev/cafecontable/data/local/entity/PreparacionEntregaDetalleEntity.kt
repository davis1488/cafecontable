package com.ethandev.cafecontable.data.local.entity

data class PreparacionEntregaDetalleEntity(
    val id: String,
    val preparacionId: String,
    val productoId: String,
    val cantidadUsada: Double,
    val costoUnit: Long
)