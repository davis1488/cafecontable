package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.PrestamoRepository

class ListarPrestamosUseCase(
    private val repository: PrestamoRepository
) {
    operator fun invoke() = repository.listarPrestamos()
}