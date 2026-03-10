package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cuenta_por_cobrar",
    indices = [Index("fecha"), Index("ventaId")]
)
data class CuentaPorCobrarEntity(
    @PrimaryKey val id: String,
    val fecha: Long,
    val ventaId: String,
    val cliente: String,
    val valorInicial: Long,
    val saldoPendiente: Long,
    val estado: String = "PENDIENTE", // PENDIENTE | PAGADO
    val nota: String? = null
)