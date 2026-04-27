package com.ethandev.cafecontable.domain.model

data class GastoOperacionModel(
    val id: String,
    val operacionId: String,
    val categoria: String,
    val descripcion: String?,
    val valor: Long,
    val tercero: String?,
    val observacion: String?,
    val fecha: Long
)