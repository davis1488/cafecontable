package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository

class ActualizarCompraUseCase(
    private val repository: HistorialCompraRepository
) {
    suspend operator fun invoke(compra: CompraCafeEntity) =
        repository.actualizarCompra(compra)
}
