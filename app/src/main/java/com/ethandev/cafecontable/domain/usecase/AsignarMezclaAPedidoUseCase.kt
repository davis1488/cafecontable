package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.AsignacionMezclaPedidoDao
import com.ethandev.cafecontable.data.local.dao.MezclaDao
import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.data.local.entity.AsignacionMezclaPedidoEntity
import com.ethandev.cafecontable.domain.constants.EstadoAnuncio
import com.ethandev.cafecontable.domain.constants.EstadoMezcla
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

//        require(mezcla.cantidadDisponible >= input.cantidadAsignada) {
//            "La mezcla no tiene saldo suficiente"
//        }
        val totalAsignado = asignacionDao.totalAsignadoPorMezcla(input.mezclaId)
        val disponible = mezcla.cantidadTotal - totalAsignado
        require(disponible >= input.cantidadAsignada) {
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

        val nuevaCantidadDisponible = disponible - input.cantidadAsignada

        val nuevoEstadoMezcla = when {
            nuevaCantidadDisponible <= 0.0 && mezcla.estado.equals(EstadoMezcla.ANALIZADO.name, ignoreCase = true) ->
                EstadoMezcla.ANALIZADO

            nuevaCantidadDisponible <= 0.0 && mezcla.estado.equals(EstadoMezcla.ENTREGADO.name, ignoreCase = true) ->
                EstadoMezcla.ENTREGADO

            else -> mezcla.estado
        }

        mezclaDao.actualizarDisponibleYEstado(
            mezclaId = mezcla.id,
            //cantidadDisponible = nuevaCantidadDisponible,
            estado = nuevoEstadoMezcla.toString()
        )

        val nuevaCantidadAsignada = pedido.cantidadAsignada + input.cantidadAsignada

        val nuevoEstadoPedido = when {
            nuevaCantidadAsignada < pedido.cantidadPactada -> EstadoAnuncio.ENTREGA_PARCIAL.valorDb
            else -> EstadoAnuncio.ENTREGA_TOTAL.valorDb
        }

        ventaPedidoDao.actualizarCantidadAsignadaYEstado(
            pedidoId = pedido.id,
            cantidadAsignada = nuevaCantidadAsignada,
            estado = nuevoEstadoPedido
        )
    }
}