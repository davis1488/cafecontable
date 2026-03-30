package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "venta_cafe",
    indices = [Index("fecha"), Index("productoId")]
)
data class VentaCafeEntity(
    @PrimaryKey val id: String,
    val fecha: Long,
    val productoId: String,
    val cantidad: Double,
    val precioUnitVenta: Long,
    val cliente: String? = null,
    val esCredito: Boolean = false,
    val nota: String? = null,
    val factor: Double,
    val cantidadPactada: Double,
    val unidad: String,
    val total: Long,
    val cantidadEntregada: Double,
    val estado: String
)

