package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository

class ListarVentasPedidoUseCase(
    private val repository: VentaPedidoRepository
) {
    suspend operator fun invoke(): List<VentaPedido> {
        return repository.listarVentas()
    }
}