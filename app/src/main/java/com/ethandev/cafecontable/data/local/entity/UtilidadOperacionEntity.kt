package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utilidad_operacion")
data class UtilidadOperacionEntity(
    @PrimaryKey
    val id: String,
    val fecha: Long,

    val liquidacionId: String,
    val mezclaId: String,
    val pedidoId: String,

    val cantidadKg: Double,

    val valorVentaBruto: Long,
    val ajusteFactor: Long,
    val descuentos: Long,
    val valorVentaNeto: Long,

    val costoCafe: Long,

    val gastosCompra: Long,
    val gastosMezcla: Long,
    val gastosEntrega: Long,
    val otrosGastos: Long,
    val totalGastos: Long,

    val utilidadBruta: Long,
    val utilidadNeta: Long,
    val margenPorcentaje: Double,

    val nota: String?
)