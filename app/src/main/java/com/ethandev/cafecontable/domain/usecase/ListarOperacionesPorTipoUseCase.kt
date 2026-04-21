package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.repository.OperacionRepository

class ListarOperacionesPorTipoUseCase(
    private val repository: OperacionRepository
) {
    suspend operator fun invoke(tipo: String): List<OperacionEntity> {
        return repository.listarPorTipo(tipo)
    }
}