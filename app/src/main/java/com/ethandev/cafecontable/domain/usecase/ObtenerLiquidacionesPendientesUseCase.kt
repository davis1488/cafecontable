package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.LiquidacionPendiente
import com.ethandev.cafecontable.domain.repository.LiquidacionRepository

class ObtenerLiquidacionesPendientesUseCase(
    private val repository: LiquidacionRepository
) {
    suspend operator fun invoke(): List<LiquidacionPendiente> {
        return repository.obtenerPendientes()
    }
}