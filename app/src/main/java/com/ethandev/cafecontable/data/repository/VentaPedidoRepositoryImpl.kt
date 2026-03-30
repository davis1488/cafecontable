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

        if (cliente.isBlank()) {
            throw IllegalArgumentException("Debes ingresar el cliente")
        }

        if (input.cantidadPactada <= 0.0) {
            throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        }

        if (input.precioUnitVenta <= 0L) {
            throw IllegalArgumentException("El precio de venta debe ser mayor a 0")
        }

        val totalVenta = (input.cantidadPactada * input.precioUnitVenta).toLong()

        db.ventaPedidoDao().insert(
            VentaPedidoEntity(
                id = UUID.randomUUID().toString(),
                fecha = System.currentTimeMillis(),
                cliente = cliente,
                cantidadPactada = input.cantidadPactada,
                unidad = input.unidad.trim().uppercase(),
                precioUnitVenta = input.precioUnitVenta,
                totalVenta = totalVenta,
                cantidadEntregada = 0.0,
                estado = "PENDIENTE_PREPARACION",
                nota = nota
            )
        )
    }

    override suspend fun listarVentas(): List<VentaPedido> {
        return db.ventaPedidoDao().getAll().map {
            VentaPedido(
                id = it.id,
                fecha = it.fecha,
                cliente = it.cliente,
                cantidadPactada = it.cantidadPactada,
                unidad = it.unidad,
                precioUnitVenta = it.precioUnitVenta,
                totalVenta = it.totalVenta,
                cantidadEntregada = it.cantidadEntregada,
                estado = it.estado,
                nota = it.nota
            )
        }
    }

    override suspend fun listarVentasPendientes(): List<VentaPedido> {
        return db.ventaPedidoDao().getVentasPendientes().map {
            VentaPedido(
                id = it.id,
                fecha = it.fecha,
                cliente = it.cliente,
                cantidadPactada = it.cantidadPactada,
                unidad = it.unidad,
                precioUnitVenta = it.precioUnitVenta,
                totalVenta = it.totalVenta,
                cantidadEntregada = it.cantidadEntregada,
                estado = it.estado,
                nota = it.nota
            )
        }
    }

    override suspend fun obtenerVentaPorId(id: String): VentaPedido? {
        return db.ventaPedidoDao().getById(id)?.let {
            VentaPedido(
                id = it.id,
                fecha = it.fecha,
                cliente = it.cliente,
                cantidadPactada = it.cantidadPactada,
                unidad = it.unidad,
                precioUnitVenta = it.precioUnitVenta,
                totalVenta = it.totalVenta,
                cantidadEntregada = it.cantidadEntregada,
                estado = it.estado,
                nota = it.nota
            )
        }
    }

    override suspend fun actualizarEntrega(
        id: String,
        cantidadEntregada: Double,
        estado: String
    ) {
        db.ventaPedidoDao().actualizarEntrega(id, cantidadEntregada, estado)
    }
}