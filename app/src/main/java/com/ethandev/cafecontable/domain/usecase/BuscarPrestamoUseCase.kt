package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.PrestamoRepository

class BuscarPrestamosUseCase(
    private val repository: PrestamoRepository
) {
    operator fun invoke(query: String) = repository.buscarPrestamos(query)
}