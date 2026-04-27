package com.ethandev.cafecontable.domain.model

data class OperacionResumenModel(
    val id: String,
    val nombre: String,
    val tipo: String,
    val descripcion: String?,
    val totalGastos: Long
)