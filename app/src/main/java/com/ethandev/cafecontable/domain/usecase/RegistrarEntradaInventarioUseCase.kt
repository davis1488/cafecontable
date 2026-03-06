package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.InventarioRepository

class RegistrarEntradaInventarioUseCase(
    private val repo: InventarioRepository
) {

    suspend operator fun invoke(
        productoId: String,
        cantidad: Double,
        costoUnit: Long,
        nota: String?
    ) {
        repo.registrarEntrada(productoId, cantidad, costoUnit, nota)
    }
}