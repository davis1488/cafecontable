package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mezcla_detalle")
data class MezclaDetalleEntity(
    @PrimaryKey
    val id: String,
    val mezclaId: String,
    val compraId: String,
    val productoId: String,
    val productoNombre: String,
    val cantidadUsada: Double,
    val costoUnitCompra: Long,
)