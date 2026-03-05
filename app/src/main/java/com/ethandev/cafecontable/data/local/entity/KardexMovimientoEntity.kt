package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kardex_mov",
    indices = [Index("productoId"), Index("fecha")]
)
data class KardexMovimientoEntity(
    @PrimaryKey val id: String,
    val fecha: Long,
    val productoId: String,
    val tipo: String,        // "ENTRADA" | "SALIDA" | "AJUSTE"
    val cantidad: Double,
    val costoUnit: Long,
    val docTipo: String? = null, // "COMPRA" | "VENTA" | "AJUSTE"
    val docId: String? = null,
    val nota: String? = null
)