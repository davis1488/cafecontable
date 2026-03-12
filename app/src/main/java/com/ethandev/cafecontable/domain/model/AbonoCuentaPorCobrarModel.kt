package com.ethandev.cafecontable.domain.model

data class AbonoCuentaPorCobrarModel(
    val id: String,
    val cuentaId: String,
    val fecha: Long,
    val valor: Long,
    val nota: String?
)
