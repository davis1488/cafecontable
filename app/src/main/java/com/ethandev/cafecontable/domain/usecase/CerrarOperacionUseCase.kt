package com.ethandev.cafecontable.domain.usecase.operacion

import com.ethandev.cafecontable.domain.repository.OperacionRepository

class CerrarOperacionUseCase(
    private val repository: OperacionRepository
) {
    suspend operator fun invoke(operacionId: String) {
        repository.cerrarOperacion(operacionId)
    }
}