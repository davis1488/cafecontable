
package com.ethandev.cafecontable.domain.model

data class EditarMezclaInput(
    val mezclaId: String,
    val nota: String?,
    val items: List<MezclaDetalleInput>
)
