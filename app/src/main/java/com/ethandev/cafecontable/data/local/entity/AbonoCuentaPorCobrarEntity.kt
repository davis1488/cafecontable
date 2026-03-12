package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "abono_cuenta_por_cobrar",
    indices = [Index("cuentaId"), Index("fecha")]
)
data class AbonoCuentaPorCobrarEntity(
    @PrimaryKey val id: String,
    val cuentaId: String,
    val fecha: Long,
    val valor: Long,
    val nota: String? = null
)