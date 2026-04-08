package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.MezclaHistorialItem
import com.ethandev.cafecontable.domain.repository.MezclaRepository

class ObtenerHistorialMezclasUseCase(
    private val repository: MezclaRepository
) {
    suspend operator fun invoke(): List<MezclaHistorialItem> {
        return repository.obtenerHistorialMezclas()
    }
}