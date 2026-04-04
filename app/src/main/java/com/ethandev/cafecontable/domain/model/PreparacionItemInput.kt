package com.ethandev.cafecontable.domain.model


data class  PreparacionItemInput(
    val productoId: String,
    val productoNombre: String,
    val cantidadUsada: Double,
    val costoUnit: Long
)
