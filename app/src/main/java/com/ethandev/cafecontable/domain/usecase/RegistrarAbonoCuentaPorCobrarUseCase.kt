package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.CuentaPorCobrarRepository
import com.ethandev.cafecontable.domain.model.RegistrarAbonoInput
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPagarInput

class RegistrarAbonoCuentaPorCobrarUseCase(
    private val repo: CuentaPorCobrarRepository
) {
    suspend operator fun invoke(input: RegistrarAbonoInput) {
        repo.registrarAbono(input)
    }
}