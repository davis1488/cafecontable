package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ethandev.cafecontable.domain.constants.EstadoOperacion

@Entity(tableName = "operacion")
data class OperacionEntity(
    @PrimaryKey
    val id: String,
    val fecha: Long,
    val tipo: String, // COMPRA, MEZCLA, DESPACHO, ADMINISTRATIVA
    val nombre: String,
    val descripcion: String?,
    val estado: EstadoOperacion = EstadoOperacion.ACTIVA
)