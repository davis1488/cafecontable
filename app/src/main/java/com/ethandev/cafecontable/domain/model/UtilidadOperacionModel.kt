package com.ethandev.cafecontable.domain.model

data class UtilidadOperacionModel(
    val id: String,
    val fecha: Long,
    val liquidacionId: String,
    val mezclaId: String,
    val pedidoId: String,
    val cliente: String?,
    val cantidadKg: Double,
    val valorVentaNeto: Long,
    val costoCafe: Long,
    val totalGastos: Long,
    val utilidadBruta: Long,
    val utilidadNeta: Long,
    val margenPorcentaje: Double,
    val nota: String?
)