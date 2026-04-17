package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.dao.PreparacionEntregaDao
import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaEntity
import com.ethandev.cafecontable.domain.constants.EstadoAnuncio
import com.ethandev.cafecontable.domain.model.RegistrarPreparacionEntregaInput
import com.ethandev.cafecontable.domain.util.calcularAjustePorFactor
import java.util.UUID

class RegistrarPreparacionEntregaUseCase(
    private val ventaPedidoDao: VentaPedidoDao,
    private val inventarioDao: InventarioDao,
    private val preparacionEntregaDao: PreparacionEntregaDao
) {

    suspend operator fun invoke(input: RegistrarPreparacionEntregaInput) {
        require(input.items.isNotEmpty()) {
            "Debes agregar al menos un producto a la mezcla"
        }

        val venta = ventaPedidoDao.obtenerPorId(input.ventaId)
            ?: throw IllegalArgumentException("No se encontró el pedido")

        val cantidadPreparada = input.items.sumOf { it.cantidadUsada }

        require(cantidadPreparada > 0.0) {
            "La cantidad preparada debe ser mayor a cero"
        }

        val pendiente = venta.cantidadPactada - venta.cantidadAsignada

        require(cantidadPreparada <= pendiente) {
            "La cantidad preparada supera el saldo pendiente"
        }

        input.items.forEach { item ->
            require(item.productoId.isNotBlank()) {
                "El productoId no puede estar vacío"
            }

            require(item.productoNombre.isNotBlank()) {
                "El nombre del producto no puede estar vacío"
            }

            require(item.cantidadUsada > 0.0) {
                "La cantidad usada de ${item.productoNombre} debe ser mayor a cero"
            }

            require(item.costoUnit >= 0L) {
                "El costo unitario de ${item.productoNombre} no puede ser negativo"
            }

            val existencia = inventarioDao.existencia(item.productoId)
            require(item.cantidadUsada <= existencia) {
                "Inventario insuficiente para ${item.productoNombre}. Disponible: $existencia"
            }
        }

        val costoTotal = input.items.sumOf { item ->
            item.cantidadUsada * item.costoUnit.toDouble()
        }.toLong()

        val costoPromedio = if (cantidadPreparada > 0.0) {
            (costoTotal / cantidadPreparada).toLong()
        } else {
            0L
        }

        val subtotalVenta = (cantidadPreparada * input.precioVentaUnitario).toLong()

        val resultadoFactor = calcularAjustePorFactor(
            subtotalVenta = subtotalVenta,
            factor = input.factor
        )

        val margen = resultadoFactor.totalFinal - costoTotal
        val preparacionId = UUID.randomUUID().toString()

        val preparacion = PreparacionEntregaEntity(
            id = preparacionId,
            ventaId = input.ventaId,
            fecha = input.fecha,
            cantidadPreparada = cantidadPreparada,
            costoTotal = costoTotal,
            costoPromedio = costoPromedio,
            precioVentaUnitario = input.precioVentaUnitario,
            subtotalVenta = subtotalVenta,
            factor = input.factor,
            ajusteFactor = resultadoFactor.ajusteFactor,
            totalFinal = resultadoFactor.totalFinal,
            margen = margen,
            estado = "REGISTRADA",
            nota = input.nota?.trim()?.ifBlank { null }
        )

        val detalles = input.items.map { item ->
            PreparacionEntregaDetalleEntity(
                id = UUID.randomUUID().toString(),
                preparacionId = preparacionId,
                productoId = item.productoId,
                productoNombre = item.productoNombre,
                cantidadUsada = item.cantidadUsada,
                costoUnit = item.costoUnit,
                subtotal = (item.cantidadUsada * item.costoUnit.toDouble()).toLong()
            )
        }

        preparacionEntregaDao.insertPreparacion(preparacion)
        preparacionEntregaDao.insertDetalles(detalles)

        input.items.forEach { item ->
            inventarioDao.insertMov(
                KardexMovimientoEntity(
                    id = UUID.randomUUID().toString(),
                    fecha = input.fecha,
                    productoId = item.productoId,
                    tipo = "SALIDA",
                    cantidad = item.cantidadUsada,
                    costoUnit = item.costoUnit,
                    docTipo = "PREPARACION_ENTREGA",
                    docId = preparacionId,
                    nota = "Salida por preparación de pedido ${venta.id}"
                )
            )
        }

        val nuevaCantidadEntregada = venta.cantidadAsignada + cantidadPreparada

        val nuevoEstado = if (nuevaCantidadEntregada < venta.cantidadPactada) {
            EstadoAnuncio.ENTREGA_PARCIAL.valorDb
        } else {
            EstadoAnuncio.ENTREGA_TOTAL.valorDb
        }

        ventaPedidoDao.actualizarCantidadAsignadaYEstado(
            pedidoId = venta.id,
            cantidadAsignada = nuevaCantidadEntregada,
            estado = nuevoEstado
        )
    }
}