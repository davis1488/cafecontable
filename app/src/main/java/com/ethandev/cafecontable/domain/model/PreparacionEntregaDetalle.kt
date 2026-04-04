package com.ethandev.cafecontable.domain.model

data class PreparacionEntregaDetalle(
    val productoId: String,
    val productoNombre: String,
    val cantidadUsada: Double,
    val costoUnit: Long,
    val subtotal: Long
)
