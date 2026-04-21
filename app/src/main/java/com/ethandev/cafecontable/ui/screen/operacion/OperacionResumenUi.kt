package com.ethandev.cafecontable.ui.screen.operacion

data class OperacionResumenUi(
    val id: String,
    val nombre: String,
    val tipo: String,
    val descripcion: String?,
    val totalGastos: Long
)