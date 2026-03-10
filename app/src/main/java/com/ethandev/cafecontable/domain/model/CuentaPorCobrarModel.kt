package com.ethandev.cafecontable.domain.model

data class CuentaPorCobrarModel(
    val id: String,
    val fecha: Long,
    val ventaId: String,
    val cliente: String,
    val valorInicial: Long,
    val saldoPendiente: Long,
    val estado: String,
    val nota: String?
)