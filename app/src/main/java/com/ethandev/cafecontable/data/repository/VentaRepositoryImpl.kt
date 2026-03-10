package com.ethandev.cafecontable.data.repository

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.CuentaPorCobrarEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.VentaCafeEntity
import com.ethandev.cafecontable.domain.repository.VentaCafeInput
import com.ethandev.cafecontable.domain.repository.VentaRepository
import java.util.UUID

class VentaRepositoryImpl(
    private val db: AppDatabase
) : VentaRepository {

    override suspend fun registrarVenta(input: VentaCafeInput) {
        db.withTransaction {
            val productoDao = db.productoDao()
            val inventarioDao = db.inventarioDao()
            val ventaDao = db.ventaDao()
            val cuentaDao = db.cuentaPorCobrarDao()

            val producto = productoDao.getByNombre(input.productoNombre.trim())
                ?: throw IllegalStateException("El producto no existe: ${input.productoNombre}")

            val existenciaActual = inventarioDao.existencia(producto.id)

            if (input.cantidad > existenciaActual) {
                throw IllegalStateException(
                    "No hay inventario suficiente. Existencia actual: $existenciaActual ${producto.unidad}"
                )
            }

            val ventaId = UUID.randomUUID().toString()

            ventaDao.insert(
                VentaCafeEntity(
                    id = ventaId,
                    fecha = System.currentTimeMillis(),
                    productoId = producto.id,
                    cantidad = input.cantidad,
                    precioUnitVenta = input.precioUnitVenta,
                    cliente = input.cliente?.trim()?.ifBlank { null },
                    esCredito = input.esCredito,
                    nota = input.nota?.trim()?.ifBlank { null },
                    factor = input.factor
                )
            )

            inventarioDao.insertMov(
                KardexMovimientoEntity(
                    id = UUID.randomUUID().toString(),
                    fecha = System.currentTimeMillis(),
                    productoId = producto.id,
                    tipo = "SALIDA",
                    cantidad = input.cantidad,
                    costoUnit = input.precioUnitVenta,
                    docTipo = "VENTA",
                    docId = ventaId,
                    nota = input.nota
                )
            )

            if (input.esCredito) {
                val cliente = input.cliente?.trim().orEmpty()
                if (cliente.isBlank()) {
                    throw IllegalStateException("Debes ingresar el nombre del cliente para ventas a crédito")
                }

                val totalVenta = (input.cantidad * input.precioUnitVenta).toLong()

                cuentaDao.insert(
                    CuentaPorCobrarEntity(
                        id = UUID.randomUUID().toString(),
                        fecha = System.currentTimeMillis(),
                        ventaId = ventaId,
                        cliente = cliente,
                        valorInicial = totalVenta,
                        saldoPendiente = totalVenta,
                        estado = "PENDIENTE",
                        nota = input.nota?.trim()?.ifBlank { null }
                    )
                )
            }
        }
    }
}