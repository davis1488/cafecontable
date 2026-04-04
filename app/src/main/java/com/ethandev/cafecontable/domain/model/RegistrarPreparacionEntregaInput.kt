package com.ethandev.cafecontable.domain.model

data class RegistrarPreparacionEntregaInput(
    val ventaId: String,
    val fecha: Long,
    val precioVentaUnitario: Long,
    val factor: Double,
    val nota: String?,
    val items: List<PreparacionItemInput>
)