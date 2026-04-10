package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.LiquidacionHistorial
import com.ethandev.cafecontable.domain.repository.LiquidacionRepository

class ObtenerHistorialLiquidacionesUseCase(
    private val repository: LiquidacionRepository
) {
    suspend operator fun invoke(): List<LiquidacionHistorial> {
        return repository.obtenerHistorial()
    }
}