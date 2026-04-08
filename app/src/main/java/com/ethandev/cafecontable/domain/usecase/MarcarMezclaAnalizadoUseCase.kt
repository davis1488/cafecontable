package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.MezclaRepository

class MarcarMezclaAnalizadoUseCase(
    private val repository: MezclaRepository
) {
    suspend operator fun invoke(
        mezclaId: String,
        factorRendimiento: Double
    ): Int {
        return repository.marcarAnalizado(
            mezclaId,
            factorRendimiento
        )
    }
}