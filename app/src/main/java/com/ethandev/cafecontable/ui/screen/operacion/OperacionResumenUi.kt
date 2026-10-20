package com.ethandev.cafecontable.ui.screen.operacion

import com.ethandev.cafecontable.domain.constants.EstadoOperacion

data class OperacionResumenUi(
    val id: String,
    val nombre: String,
    val tipo: String,
    val descripcion: String?,
    val totalGastos: Long,
    val estado: EstadoOperacion

)
