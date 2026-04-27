package com.ethandev.cafecontable.ui.screen.operacion

import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.constants.TipoOperacion

data class OperacionGastoState(
    val operaciones: List<OperacionEntity> = emptyList(),
    val gastosPorOperacion: List<GastoPorOperacionUi> = emptyList(),
    val tipoOperacionSeleccionado: String = TipoOperacion.COMPRA,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

data class GastoPorOperacionUi(
    val operacionId: String,
    val nombreOperacion: String,
    val tipo: String,
    val descripcionOperacion: String?,
    val totalGastos: Long,
    val gastos: List<GastoUi>
)

data class GastoUi(
    val id: String,
    val categoria: String,
    val descripcion: String?,
    val valor: Long,
    val tercero: String?,
    val observacion: String?
)