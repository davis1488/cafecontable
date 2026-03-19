package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.PrestamoRepository

class ListarAbonosPrestamoUseCase(
    private val repository: PrestamoRepository
) {
    operator fun invoke(prestamoId: Int) = repository.listarAbonos(prestamoId)
}