package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository

class MarcarVentaPedidoComoEntregadoUseCase(
    private val repository: VentaPedidoRepository

) {
    suspend operator fun invoke(
        pedidoId: String,
        numeroSacos: Int,
        pesoNeto: Double,
        pesoBruto: Double
    ) {
        require(pedidoId.isNotBlank()) { "El pedido es obligatorio" }
        require(numeroSacos > 0) { "El número de sacos debe ser mayor a cero" }
        require(pesoNeto > 0.0) { "El peso neto debe ser mayor a cero" }
        require(pesoBruto > 0.0) { "El peso bruto debe ser mayor a cero" }
        require(pesoBruto >= pesoNeto) { "El peso bruto no puede ser menor que el peso neto" }

        val pedido = repository.obtenerPorId(pedidoId)
            ?: throw IllegalArgumentException("No se encontró el pedido")

        require(
            pedido.estado.equals("PENDIENTE_ASIGNACION", ignoreCase = true) ||
                    pedido.estado.equals("ASIGNACION_PARCIAL", ignoreCase = true) ||
                    pedido.estado.equals("ASIGNADO", ignoreCase = true)
        ) {
            "Solo se puede pasar a ENTREGADO desde PENDIENTE_ASIGNACION, ASIGNACION_PARCIAL o ASIGNADO"
        }

        val filas = repository.actualizarEntrega(
            pedidoId = pedidoId,
            estado = "ENTREGADO",
            numeroSacos = numeroSacos,
            pesoNeto = pesoNeto,
            pesoBruto = pesoBruto
        )

        if (filas == 0) {
            throw IllegalStateException("No fue posible marcar el pedido como entregado")
        }
    }
}