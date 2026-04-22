package com.ethandev.cafecontable.domain.model

data class AgregarComprasAMezclaInput(
    val mezclaId: String,
    val items: List<MezclaDetalleInput>
)