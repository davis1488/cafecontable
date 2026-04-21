package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gasto")
data class GastoEntity(
    @PrimaryKey val id: String,
    val fecha: Long,
    val tipo: String, // GENERAL, LIQUIDACION, MEZCLA, PEDIDO
    val referenciaId: String?, // id de liquidacion, mezcla o pedido si aplica
    val categoria: String, // TRANSPORTE, CARGUE, DESCARGUE, EMPAQUE, COMISION, OTRO
    val descripcion: String?,
    val valor: Long,
    val tercero: String?,
    val observacion: String?,
    val estado: String = "ACTIVO"
)