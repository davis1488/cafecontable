package com.ethandev.cafecontable.domain.model

data class AbonoPrestamoModel(
    val id: Int = 0,
    val prestamoId: Int,
    val fechaAbono: Long,
    val valorAbono: Double,
    val observacion: String = ""
)