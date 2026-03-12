package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cuenta_por_pagar",
    indices = [Index("fecha"), Index("compraId")]
)
data class CuentaPorPagarEntity(
    @PrimaryKey val id: String,
    val fecha: Long,
    val compraId: String,
    val proveedor: String,
    val valorInicial: Long,
    val saldoPendiente: Long,
    val estado: String = "PENDIENTE", // PENDIENTE | PAGADO
    val nota: String? = null
)