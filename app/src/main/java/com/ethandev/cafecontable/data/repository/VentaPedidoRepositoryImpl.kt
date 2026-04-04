package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.VentaPedidoEntity
import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.VentaPedidoInput
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository
import java.util.UUID

class VentaPedidoRepositoryImpl(
    private val db: AppDatabase
) : VentaPedidoRepository {

    override suspend fun registrarVenta(input: VentaPedidoInput) {
        val cliente = input.cliente.trim()
        val nota = input.nota?.trim()?.ifBlank { null }

        require(cliente.isNotBlank()) {
            "Debes ingresar el cliente"
        }

        require(input.cantidadPactada > 0.0) {
            "La cantidad debe ser mayor a 0"
        }

        require(input.precioUnitVenta > 0L) {
            "El precio de venta debe ser mayor a 0"
        }

        val unidad = input.unidad.trim().uppercase()
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
                estado = "PENDIENTE_ASIGNACION",
                nota = nota
            )
        )
    }

    override suspend fun listarVentas(): List<VentaPedido> {
        return db.ventaPedidoDao().getAll().map { pedido ->
            pedido.toDomain()
        }
    }

    override suspend fun listarVentasPendientes(): List<VentaPedido> {
        return db.ventaPedidoDao().getPendientes().map { pedido ->
            pedido.toDomain()
        }
    }

    override suspend fun obtenerVentaPorId(id: String): VentaPedido? {
        return db.ventaPedidoDao().obtenerPorId(id)?.toDomain()
    }

    override suspend fun actualizarAsignacion(
        id: String,
        cantidadAsignada: Double,
        estado: String
    ) {
        db.ventaPedidoDao().actualizarEntregaYEstado(
            pedidoId = id,
            cantidadAsignada = cantidadAsignada,
            estado = estado
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