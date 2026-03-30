package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.VentaPedidoInput
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository

class RegistrarVentaPedidoUseCase(
    private val repository: VentaPedidoRepository
) {
    suspend operator fun invoke(input: VentaPedidoInput) {
        repository.registrarVenta(input)
    }
}