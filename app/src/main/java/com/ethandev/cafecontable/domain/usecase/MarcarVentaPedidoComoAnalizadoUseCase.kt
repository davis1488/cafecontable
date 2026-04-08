package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository
import kotlin.math.abs

class MarcarVentaPedidoComoAnalizadoUseCase(
    private val repository: VentaPedidoRepository
) {
    suspend operator fun invoke(
        pedidoId: String,
        factor: Double
    ) {
        require(pedidoId.isNotBlank()) { "El pedido es obligatorio" }
        require(factor > 0.0) { "El factor debe ser mayor a cero" }

        val pedido = repository.obtenerPorId(pedidoId)
            ?: throw IllegalArgumentException("No se encontró el pedido")

        require(pedido.estado.equals("ENTREGADO", ignoreCase = true)) {
            "Solo se puede pasar a ANALIZADO desde ENTREGADO"
        }

        val ajusteAnalisis = calcularAjusteAnalisis(
            precioBase = pedido.precioUnitVenta,
            factor = factor
        )

        val filas = repository.actualizarAnalisis(
            pedidoId = pedidoId,
            estado = "ANALIZADO",
            factorAnalisis = factor,
            ajusteAnalisis = ajusteAnalisis.toDouble()
        )

        if (filas == 0) {
            throw IllegalStateException("No fue posible marcar el pedido como analizado")
        }
    }

    private fun calcularAjusteAnalisis(
        precioBase: Long,
        factor: Double
    ): Long {
        val diferencia = abs(factor - 90.0) / 100.0

        return when {
            factor > 90.0 -> -(precioBase * diferencia).toLong() // descuento
            factor < 90.0 -> (precioBase * diferencia).toLong()  // bonificación
            else -> 0L
        }
    }
}