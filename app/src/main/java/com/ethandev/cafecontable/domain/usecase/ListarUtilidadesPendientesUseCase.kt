package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.UtilidadOperacionRepository

class ListarUtilidadesPendientesUseCase(
    private val repository: UtilidadOperacionRepository
) {
    suspend operator fun invoke() = repository.listarPendientes()
}