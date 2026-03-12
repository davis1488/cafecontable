package com.ethandev.cafecontable.domain.model

data class RegistrarAbonoInput(
    val cuentaId: String,
    val valor: Long,
    val nota: String?
)
