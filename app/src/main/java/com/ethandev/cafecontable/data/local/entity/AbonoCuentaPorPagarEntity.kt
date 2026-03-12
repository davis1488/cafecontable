package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "abono_cuenta_por_pagar",
    indices = [Index("cuentaId"), Index("fecha")]
)
data class AbonoCuentaPorPagarEntity(
    @PrimaryKey val id: String,
    val cuentaId: String,
    val fecha: Long,
    val valor: Long,
    val nota: String? = null
)