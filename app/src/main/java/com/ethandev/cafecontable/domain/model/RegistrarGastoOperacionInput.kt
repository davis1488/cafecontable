package com.ethandev.cafecontable.domain.model

data class RegistrarGastoOperacionInput(
    val operacionId: String,
    val categoria: String,
    val descripcion: String?,
    val valor: Long,
    val tercero: String?,
    val observacion: String?
)