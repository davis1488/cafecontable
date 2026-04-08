package com.ethandev.cafecontable.domain.model

data class MezclaHistorialItem(
    val id: String,
    val fechaTexto: String,
    val cantidadTotal: Double,
    val costoTotal: Long,
    val estado: String,
    val nota: String?
)