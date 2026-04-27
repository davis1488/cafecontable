package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.GastoOperacionModel
import com.ethandev.cafecontable.domain.repository.GastoOperacionRepository

class ListarGastosPorOperacionUseCase(
    private val repository: GastoOperacionRepository
) {

    suspend operator fun invoke(
        operacionId: String
    ): List<GastoOperacionModel> {
        return repository.listarGastosPorOperacion(operacionId)
    }
}