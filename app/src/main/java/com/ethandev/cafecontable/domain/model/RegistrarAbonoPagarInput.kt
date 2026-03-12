package com.ethandev.cafecontable.domain.model

data class RegistrarAbonoPagarInput(
    val cuentaId: String,
    val valor:Long,
    val nota:String?
)
