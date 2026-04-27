package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.LiquidacionRepository
import com.ethandev.cafecontable.domain.repository.dto.RegistrarLiquidacionInput

class RegistrarLiquidacionUseCase(
    private val repository: LiquidacionRepository
) {

    suspend operator fun invoke(input: RegistrarLiquidacionInput) {
        repository.registrarLiquidacion(input)
    }
}