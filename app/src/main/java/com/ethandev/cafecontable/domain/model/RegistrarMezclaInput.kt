package com.ethandev.cafecontable.domain.model

data class RegistrarMezclaInput(
    val fecha: Long,
    val nota: String?,
    val items: List<MezclaDetalleInput>
)