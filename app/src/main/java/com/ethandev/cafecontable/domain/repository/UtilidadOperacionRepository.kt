package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.UtilidadOperacionModel
import com.ethandev.cafecontable.domain.model.UtilidadPendienteModel

interface UtilidadOperacionRepository {
    suspend fun listarPendientes(): List<UtilidadPendienteModel>
    suspend fun listarHistorial(): List<UtilidadOperacionModel>
    suspend fun calcularYGuardarUtilidad(
        pendiente: UtilidadPendienteModel,
        nota: String?
    )

    suspend fun obtenerResumen(): ResumenUtilidadModel
}

data class ResumenUtilidadModel(
    val ventasNetas: Long,
    val costoCafe: Long,
    val gastos: Long,
    val utilidadNeta: Long,
    val margenPorcentaje: Double
)