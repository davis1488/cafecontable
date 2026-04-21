package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "abonos_prestamo")
data class AbonoPrestamoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val prestamoId: Int,
    val fechaAbono: Long,
    val valorAbono: Double,
    val observacion: String = ""
)