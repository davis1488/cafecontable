package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.UtilidadOperacionRepository

class ObtenerResumenUtilidadUseCase(
    private val repository: UtilidadOperacionRepository
) {
    suspend operator fun invoke() = repository.obtenerResumen()
}