package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.CuentaPorPagarModel
import com.ethandev.cafecontable.domain.repository.CuentaPorPagarRepository

class ListarCuentasPorPagarUseCase(
    private val repo: CuentaPorPagarRepository
) {
    suspend operator fun invoke(): List<CuentaPorPagarModel> {
        return repo.listarPendientes()
    }
}