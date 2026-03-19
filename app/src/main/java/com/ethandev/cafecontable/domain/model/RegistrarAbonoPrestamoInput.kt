package com.ethandev.cafecontable.domain.model

data class RegistrarAbonoPrestamoInput(
    val prestamoId: Int,
    val fechaAbono: Long,
    val valorAbono: Double,
    val observacion: String = ""
)