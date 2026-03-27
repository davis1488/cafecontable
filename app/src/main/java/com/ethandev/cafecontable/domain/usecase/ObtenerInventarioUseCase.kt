package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.InventarioItemModel
import com.ethandev.cafecontable.domain.repository.InventarioRepository

class ObtenerInventarioUseCase(
    private val repository: InventarioRepository
) {
    suspend operator fun invoke(): List<InventarioItemModel> {
        return repository.obtenerInventario()
    }
}