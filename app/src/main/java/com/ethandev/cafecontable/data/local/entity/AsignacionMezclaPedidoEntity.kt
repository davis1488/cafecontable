package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asignacion_mezcla_pedido")
data class AsignacionMezclaPedidoEntity(
    @PrimaryKey
    val id: String,
    val pedidoId: String,
    val mezclaId: String,
    val fecha: Long,
    val cantidadAsignada: Double,
    val precioUnitVenta: Long,
    val subtotalVenta: Long,
    val factor: Double,
    val ajusteFactor: Long,
    val totalFinal: Long,
    val nota: String?
)