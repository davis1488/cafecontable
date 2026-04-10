package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liquidacion_entrega")
data class LiquidacionEntregaEntity(
    @PrimaryKey
    val id: String,
    val asignacionId: String,
    val pedidoId: String,
    val mezclaId: String,
    val fecha: Long,

    val cliente: String?,
    val cantidadKg: Double,
    val precioBaseKg: Long,
    val factorReal: Double,

    val valorBase: Long,
    val ajusteFactor: Long,
    val descuentoCooperativa: Long,
    val otrosDescuentos: Long,
    val valorNeto: Long,

    val nota: String?
)