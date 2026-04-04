package com.ethandev.cafecontable.data.local.entity

data class MezclaDisponibleDb(
    val mezclaId: String,
    val descripcion: String,
    val cantidadDisponible: Double,
    val cantidadTotal: Double,
    val costoPromedioKg: Long
)