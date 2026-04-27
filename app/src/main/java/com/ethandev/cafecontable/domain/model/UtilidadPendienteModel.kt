package com.ethandev.cafecontable.domain.model

data class UtilidadPendienteModel(
    val liquidacionId: String,
    val mezclaId: String,
    val pedidoId: String,
    val cliente: String?,
    val cantidadKg: Double,
    val valorVentaBruto: Long,
    val ajusteFactor: Long,
    val descuentos: Long,
    val valorVentaNeto: Long,
    val costoPromedioKg: Long,

    val gastosCompra: Long = 0L,
    val gastosMezcla: Long = 0L,
    val gastosEntrega: Long = 0L,
    val totalGastos: Long = 0L,

    val operacionCompraId: String?,
    val operacionMezclaId: String?,
    val operacionEntregaId: String?,
    )
