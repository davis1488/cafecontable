package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.model.OperacionResumenModel

interface OperacionRepository {

    // 🔹 Para lógica interna (si aún lo necesitas)
    suspend fun listarPorTipo(tipo: String): List<OperacionEntity>

    // 🔹 Para UI (la que estás usando ahora)
    suspend fun listarOperaciones(): List<OperacionResumenModel>
}