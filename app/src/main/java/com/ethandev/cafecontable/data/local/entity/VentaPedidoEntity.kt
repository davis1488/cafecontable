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
    val cantidadAsignada: Double,
    val estado: String,
    val nota: String?,

    val numeroSacos: Int = 0,
    val pesoNeto: Double = 0.0,
    val pesoBruto: Double = 0.0,
    val factorAnalisis: Double? = null,
    val ajusteAnalisis: Long? = null
)