package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository
import com.ethandev.cafecontable.domain.repository.dto.VentaPedidoInput

class RegistrarVentaPedidoUseCase(
    private val repository: VentaPedidoRepository
) {
    suspend operator fun invoke(input: VentaPedidoInput) {
        repository.registrarVenta(input)
    }
}