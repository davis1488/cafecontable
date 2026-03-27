package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository

class ActualizarMovimientoKardexUseCase(
    private val repository: HistorialCompraRepository
) {
    suspend operator fun invoke(movimiento: KardexMovimientoEntity) {
        repository.actualizarMovimientoKardex(movimiento)
    }
}