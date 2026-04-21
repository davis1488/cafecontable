package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.LiquidacionHistorial
import com.ethandev.cafecontable.domain.model.LiquidacionPendiente
import com.ethandev.cafecontable.domain.repository.dto.RegistrarLiquidacionInput

interface LiquidacionRepository {
    suspend fun obtenerPendientes(): List<LiquidacionPendiente>
    suspend fun obtenerHistorial(): List<LiquidacionHistorial>
    suspend fun registrarLiquidacion(input: RegistrarLiquidacionInput)
}