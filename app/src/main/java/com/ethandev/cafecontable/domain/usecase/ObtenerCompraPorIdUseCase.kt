package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository

class ObtenerCompraPorIdUseCase(
    private val repository: HistorialCompraRepository
) {
    suspend operator fun invoke(id: Int) =
        repository.obtenerCompraPorId(id)
}