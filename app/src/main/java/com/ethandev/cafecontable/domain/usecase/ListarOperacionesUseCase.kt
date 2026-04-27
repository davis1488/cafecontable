package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.OperacionResumenModel
import com.ethandev.cafecontable.domain.repository.OperacionRepository

class ListarOperacionesUseCase(
    private val repository: OperacionRepository
) {

    suspend operator fun invoke(): List<OperacionResumenModel> {
        return repository.listarOperaciones()
    }
}