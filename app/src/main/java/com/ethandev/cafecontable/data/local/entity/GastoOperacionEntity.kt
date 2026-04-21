package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gasto_operacion")
data class GastoOperacionEntity(
    @PrimaryKey
    val id: String,
    val operacionId: String,
    val fecha: Long,
    val categoria: String,
    val descripcion: String?,
    val valor: Long,
    val tercero: String?,
    val observacion: String?,
    val estado: String = "ACTIVO"
)