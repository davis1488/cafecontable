package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.VentaCafeInput
import com.ethandev.cafecontable.domain.repository.VentaRepository

class RegistrarVentaCafeUseCase(
    private val repo: VentaRepository
) {
    suspend operator fun invoke(input: VentaCafeInput) {
        repo.registrarVenta(input)
    }
}