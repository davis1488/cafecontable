package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.VentaPedidoEntity
import com.ethandev.cafecontable.domain.constants.EstadoAnuncio
import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository
import com.ethandev.cafecontable.domain.repository.dto.VentaPedidoInput
import java.util.UUID

class VentaPedidoRepositoryImpl(
    private val db: AppDatabase
) : VentaPedidoRepository {

    override suspend fun actualizarEstado(
        pedidoId: String,
        estado: String
    ): Int {
        return db.ventaPedidoDao().actualizarEstado(
            pedidoId = pedidoId,
            estado = estado
        )
    }

    override suspend fun obtenerPorId(id: String): VentaPedido? {
        return db.ventaPedidoDao().obtenerPorId(id)?.toDomain()
    }

    override suspend fun actualizarEntrega(
        pedidoId: String,
        estado: String,
        numeroSacos: Int,
        pesoNeto: Double,
        pesoBruto: Double
    ): Int {
        return db.ventaPedidoDao().actualizarEntrega(
            pedidoId = pedidoId,
            estado = estado,
            numeroSacos = numeroSacos,
            pesoNeto = pesoNeto,
            pesoBruto = pesoBruto
        )
    }

    override suspend fun actualizarAnalisis(
        pedidoId: String,
        estado: String,
        factorAnalisis: Double,
        ajusteAnalisis: Double
    ): Int {
        return db.ventaPedidoDao().actualizarAnalisis(
            pedidoId = pedidoId,
            estado = estado,
            factorAnalisis = factorAnalisis,
            ajusteAnalisis = ajusteAnalisis.toLong()
        )
    }

    override suspend fun registrarVenta(input: VentaPedidoInput) {
        val cliente = input.cliente.trim()
        val nota = input.nota?.trim()?.ifBlank { null }
        val unidad = input.unidad.trim().uppercase()

        require(cliente.isNotBlank()) { "Debes ingresar el cliente" }
        require(input.cantidadPactada > 0.0) { "La cantidad debe ser mayor a 0" }
        require(input.precioUnitVenta > 0L) { "El precio de venta debe ser mayor a 0" }

        val totalVenta = (input.cantidadPactada * input.precioUnitVenta).toLong()

        db.ventaPedidoDao().insert(
            VentaPedidoEntity(
                id = UUID.randomUUID().toString(),
                fecha = System.currentTimeMillis(),
                cliente = cliente,
                cantidadPactada = input.cantidadPactada,
                unidad = unidad,
                precioUnitVenta = input.precioUnitVenta,
                totalVenta = totalVenta,
                cantidadAsignada = 0.0,
                estado = EstadoAnuncio.CREADO.valorDb,
                nota = nota
            )
        )
    }

    override suspend fun listarVentas(): List<VentaPedido> {
        return db.ventaPedidoDao().getAll().map { it.toDomain() }
    }

    override suspend fun listarVentasPendientes(): List<VentaPedido> {
        return db.ventaPedidoDao().getPendientes().map { it.toDomain() }
    }

    override suspend fun obtenerVentaPorId(id: String): VentaPedido? {
        return db.ventaPedidoDao().obtenerPorId(id)?.toDomain()
    }

    override suspend fun actualizarAsignacion(
        id: String,
        cantidadAsignada: Double,
        estado: String
    ) {
        db.ventaPedidoDao().actualizarCantidadAsignadaYEstado(
            pedidoId = id,
            cantidadAsignada = cantidadAsignada,
            estado = estado
        )
    }

    override suspend fun marcarComoEntregado(id: String) {
        val pedido = db.ventaPedidoDao().obtenerPorId(id)
            ?: throw IllegalStateException("No existe el pedido")

        if (pedido.cantidadAsignada <= 0.0) {
            throw IllegalStateException("No puedes pasar a entrega total un pedido sin cantidad asignada")
        }

        db.ventaPedidoDao().actualizarEstado(
            pedidoId = id,
            estado = EstadoAnuncio.ENTREGA_TOTAL.valorDb
        )
    }

    override suspend fun marcarComoAnalizado(id: String) {
        throw UnsupportedOperationException(
            "El estado ANALIZADO ya no aplica para pedidos/anuncios. Usa estados de mezcla para ese flujo."
        )
    }

    override suspend fun finalizarVenta(id: String) {
        val pedido = db.ventaPedidoDao().obtenerPorId(id)
            ?: throw IllegalStateException("No existe el pedido")

        val estadoActual = EstadoAnuncio.from(pedido.estado)

        if (estadoActual != EstadoAnuncio.ENTREGA_TOTAL) {
            throw IllegalStateException("Solo puedes liquidar pedidos en ENTREGA_TOTAL")
        }

        db.ventaPedidoDao().actualizarEstado(
            pedidoId = id,
            estado = EstadoAnuncio.LIQUIDADO.valorDb
        )
    }

    private fun VentaPedidoEntity.toDomain(): VentaPedido {
        return VentaPedido(
            id = id,
            fecha = fecha,
            cliente = cliente,
            cantidadPactada = cantidadPactada,
            unidad = unidad,
            precioUnitVenta = precioUnitVenta,
            totalVenta = totalVenta,
            cantidadAsignada = cantidadAsignada,
            estado = estado,
            nota = nota
        )
    }
}