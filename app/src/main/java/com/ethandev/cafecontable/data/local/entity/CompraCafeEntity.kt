package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "compra_cafe",
    indices = [
        Index(value = ["fecha"]),
        Index(value = ["productoId"])
    ]
)

data class CompraCafeEntity(
    @PrimaryKey val id: String,
    val fecha: Long,
    val productoId: String,
    val cantidad: Double,
    val precioUnitCompra: Long,
    val proveedor: String? = null,
    val esCredito: Boolean = false,
    val nota: String? = null
)


