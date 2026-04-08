package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository

class FinalizarVentaPedidoUseCase(
    private val repository: VentaPedidoRepository
) {
    suspend operator fun invoke(pedidoId: String) {
        require(pedidoId.isNotBlank()) { "El pedido es obligatorio" }

        val pedido = repository.obtenerPorId(pedidoId)
            ?: throw IllegalArgumentException("No se encontró el pedido")

        require(pedido.estado.equals("ANALIZADO", ignoreCase = true)) {
            "Solo se puede finalizar un pedido que esté ANALIZADO"
        }

        val filas = repository.actualizarEstado(
            pedidoId = pedidoId,
            estado = "FINALIZADO"
        )

        if (filas == 0) {
            throw IllegalStateException("No fue posible finalizar el pedido")
        }
    }
}