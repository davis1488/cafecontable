package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository

class ObtenerMovimientoKardexPorCompraIdUseCase(
    private val repository: HistorialCompraRepository
) {
    suspend operator fun invoke(compraId: Int): KardexMovimientoEntity? {
        return repository.obtenerMovimientoKardexPorCompraId(compraId)
    }
}