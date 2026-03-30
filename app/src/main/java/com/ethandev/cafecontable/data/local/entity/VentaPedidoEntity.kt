package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venta_pedido")
data class VentaPedidoEntity(
    @PrimaryKey
    val id: String,
    val fecha: Long,
    val cliente: String,
    val cantidadPactada: Double,
    val unidad: String,
    val precioUnitVenta: Long,
    val totalVenta: Long,
    val cantidadEntregada: Double,
    val estado: String,
    val nota: String?
)