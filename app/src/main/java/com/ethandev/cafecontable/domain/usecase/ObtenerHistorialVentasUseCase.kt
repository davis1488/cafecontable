package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.VentaHistorialItem
import com.ethandev.cafecontable.domain.repository.HistorialVentaRepository

class ObtenerHistorialVentasUseCase(
    private val repo: HistorialVentaRepository
) {
    suspend operator fun invoke(): List<VentaHistorialItem> {
        return repo.obtenerHistorialVentas()
    }
}