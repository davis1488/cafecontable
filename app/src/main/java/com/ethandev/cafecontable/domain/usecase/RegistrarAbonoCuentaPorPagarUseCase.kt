package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.RegistrarAbonoInput
import com.ethandev.cafecontable.domain.repository.CuentaPorPagarRepository
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPagarInput

class RegistrarAbonoCuentaPorPagarUseCase(
    private val repo: CuentaPorPagarRepository
) {
    suspend operator fun invoke(input: RegistrarAbonoInput) {
        repo.registrarAbono(input)
    }
}