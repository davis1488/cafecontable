package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.CuentaPorCobrarModel
import com.ethandev.cafecontable.domain.repository.CuentaPorCobrarRepository

class ListarCuentasPorCobrarUseCase(
    private val repo: CuentaPorCobrarRepository
) {
    suspend operator fun invoke(): List<CuentaPorCobrarModel> {
        return repo.listarPendientes()
    }
}