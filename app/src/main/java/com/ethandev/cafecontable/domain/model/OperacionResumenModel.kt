package com.ethandev.cafecontable.domain.model

import com.ethandev.cafecontable.domain.constants.EstadoOperacion


data class OperacionResumenModel(
    val id: String,
    val nombre: String,
    val tipo: String,
    val descripcion: String?,
    val totalGastos: Long,
    val estado: EstadoOperacion,
)