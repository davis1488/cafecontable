package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.UtilidadOperacionRepository

class CalcularUtilidadUseCase(
    private val repository: UtilidadOperacionRepository
) {
    suspend operator fun invoke(
        pendiente: com.ethandev.cafecontable.domain.model.UtilidadPendienteModel,
        nota: String?
    ) {
        repository.calcularYGuardarUtilidad(pendiente, nota)
    }
}