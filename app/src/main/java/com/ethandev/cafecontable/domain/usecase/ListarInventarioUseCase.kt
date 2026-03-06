package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.InventarioItemModel
import com.ethandev.cafecontable.domain.repository.InventarioRepository

class ListarInventarioUseCase(private val repo: InventarioRepository) {
    suspend operator fun  invoke(): List<InventarioItemModel> = repo.listarInventario()
}