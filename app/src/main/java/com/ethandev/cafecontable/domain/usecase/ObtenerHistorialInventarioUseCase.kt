package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.MovimientoInventarioModel
import com.ethandev.cafecontable.domain.repository.InventarioRepository

class ObtenerHistorialInventarioUseCase(
    private val repository: InventarioRepository
) {
    suspend operator fun invoke(): List<MovimientoInventarioModel> = repository.obtenerHistorial()
}
