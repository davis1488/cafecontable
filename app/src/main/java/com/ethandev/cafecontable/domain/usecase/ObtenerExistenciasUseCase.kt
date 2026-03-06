package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.InventarioRepository

class ObtenerExistenciaUseCase(
    private val repo: InventarioRepository
) {

    suspend operator fun invoke(productoId: String): Double {
        return repo.existencia(productoId)
    }
}