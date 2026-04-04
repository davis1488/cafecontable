package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.AsignacionMezclaPedidoDao
import com.ethandev.cafecontable.data.local.dao.MezclaDao
import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.data.local.entity.AsignacionMezclaPedidoEntity
import com.ethandev.cafecontable.domain.model.AsignarMezclaAPedidoInput
import com.ethandev.cafecontable.domain.util.calcularAjustePorFactor
import java.util.UUID

class AsignarMezclaAPedidoUseCase(
    private val ventaPedidoDao: VentaPedidoDao,
    private val mezclaDao: MezclaDao,
    private val asignacionDao: AsignacionMezclaPedidoDao
) {

    suspend operator fun invoke(input: AsignarMezclaAPedidoInput) {
        require(input.pedidoId.isNotBlank()) { "El pedido es obligatorio" }
        require(input.mezclaId.isNotBlank()) { "La mezcla es obligatoria" }
        require(input.cantidadAsignada > 0.0) { "La cantidad asignada debe ser mayor a cero" }
        require(input.precioUnitVenta > 0L) { "El precio de venta debe ser mayor a cero" }

        val pedido = ventaPedidoDao.obtenerPorId(input.pedidoId)
            ?: throw IllegalArgumentException("No se encontró el pedido")

        val mezcla = mezclaDao.getById(input.mezclaId)
            ?: throw IllegalArgumentException("No se encontró la mezcla")

        require(mezcla.cantidadDisponible >= input.cantidadAsignada) {
            "La mezcla no tiene saldo suficiente"
        }

        val pendientePedido = pedido.cantidadPactada - pedido.cantidadAsignada

        require(input.cantidadAsignada <= pendientePedido) {
            "La cantidad asignada supera el saldo pendiente del anuncio"
        }

        val subtotalVenta = (input.cantidadAsignada * input.precioUnitVenta).toLong()

        val resultadoFactor = calcularAjustePorFactor(
            subtotalVenta = subtotalVenta,
            factor = input.factor
        )

        val asignacion = AsignacionMezclaPedidoEntity(
            id = UUID.randomUUID().toString(),
            pedidoId = input.pedidoId,
            mezclaId = input.mezclaId,
            fecha = input.fecha,
            cantidadAsignada = input.cantidadAsignada,
            precioUnitVenta = input.precioUnitVenta,
            subtotalVenta = subtotalVenta,
            factor = input.factor,
            ajusteFactor = resultadoFactor.ajusteFactor,
            totalFinal = resultadoFactor.totalFinal,
            nota = input.nota?.trim()?.ifBlank { null }
        )

        asignacionDao.insert(asignacion)

        val nuevaCantidadDisponible = mezcla.cantidadDisponible - input.cantidadAsignada
        val nuevoEstadoMezcla = if (nuevaCantidadDisponible <= 0.0) "AGOTADA" else "DISPONIBLE"

        mezclaDao.actualizarDisponibleYEstado(
            mezclaId = mezcla.id,
            cantidadDisponible = nuevaCantidadDisponible,
            estado = nuevoEstadoMezcla
        )

        val nuevaCantidadAsignada = pedido.cantidadAsignada + input.cantidadAsignada
        val nuevoEstadoPedido = when {
            nuevaCantidadAsignada <= 0.0 -> "PENDIENTE_ASIGNACION"
            nuevaCantidadAsignada < pedido.cantidadPactada -> "ASIGNACION_PARCIAL"
            else -> "ASIGNADO"
        }

        ventaPedidoDao.actualizarEntregaYEstado(
            pedidoId = pedido.id,
            cantidadAsignada = nuevaCantidadAsignada,
            estado = nuevoEstadoPedido
        )
    }
}