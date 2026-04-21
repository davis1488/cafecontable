package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prestamos")
data class PrestamoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombrePersona: String,
    val cedulaPersona: String,
    val fechaPrestamo: Long,
    val valorPrestado: Double,
    val saldoPendiente: Double,
    val interes: Double = 0.0,
    val observacion: String = "",
    val fechaVencimiento: Long? = null,
    val estado: String = "PENDIENTE"
)