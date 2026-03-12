
package com.ethandev.cafecontable.domain.model

data class CuentaPorPagarModel(
    val id: String,
    val fecha: Long,
    val compraId: String,
    val proveedor: String,
    val valorInicial: Long,
    val saldoPendiente: Long,
    val estado: String,
    val nota: String?
)