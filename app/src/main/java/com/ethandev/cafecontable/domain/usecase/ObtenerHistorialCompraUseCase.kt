package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository

class ObtenerHistorialComprasUseCase(
    private val repo: HistorialCompraRepository
) {
    suspend operator fun invoke(): List<CompraHistorialItem> {
        return repo.obtenerHistorialCompras()
    }
}