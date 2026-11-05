package com.ethandev.cafecontable.data.repository

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.dao.CuentaPorPagarDao
import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository
import java.util.UUID

class HistorialCompraRepositoryImpl(
    private val compraDao: CompraDao,
    private val inventarioDao: InventarioDao,
    private val cuentaPorPagarDao: CuentaPorPagarDao,
    private val db: AppDatabase
) : HistorialCompraRepository {

    override suspend fun obtenerHistorialCompras(): List<CompraHistorialItem> {
        return compraDao.historial().map {
            CompraHistorialItem(
                id = it.id,
                fecha = it.fecha,
                productoNombre = it.productoNombre,
                unidad = it.unidad,
                cantidad = it.cantidad,
                precioUnitCompra = it.precioUnitCompra,
                proveedor = it.proveedor,
                esCredito = it.esCredito,
                nota = it.nota,
                estado = it.estado
            )
        }
    }

    override suspend fun anularCompra(id: Int) {
        db.withTransaction {
            val compra = compraDao.obtenerPorId(id)
                ?: throw IllegalArgumentException("La compra no existe")

            require(compra.estado == "ACTIVA") {
                "La compra ya está anulada o no está activa"
            }

            require(compraDao.contarMezclasDeCompra(id) == 0) {
                "No puedes anular esta compra porque ya fue utilizada en una mezcla"
            }

            // Se verifica la deuda antes de afectar el Kardex.
            val cuenta = if (compra.esCredito) {
                cuentaPorPagarDao.obtenerPorCompraId(id.toString())
                    ?: throw IllegalStateException(
                        "No se encontró la cuenta por pagar de esta compra"
                    )
            } else {
                null
            }

            if (cuenta != null) {
                require(cuentaPorPagarDao.contarAbonos(cuenta.id) == 0) {
                    "No se puede anular esta compra porque tiene abonos registrados"
                }
                require(cuenta.estado == "PENDIENTE") {
                    "La cuenta por pagar no está pendiente"
                }
                require(cuenta.saldoPendiente == cuenta.valorInicial) {
                    "La cuenta tiene pagos o ajustes no registrados como abonos; revisa la deuda"
                }
            }

            val movimientoOriginal = inventarioDao.obtenerMovimientoPorDocumento(
                docTipo = "COMPRA",
                docId = id.toString()
            ) ?: throw IllegalStateException(
                "No se encontró el movimiento original del inventario"
            )

            require(
                movimientoOriginal.tipo == "ENTRADA" &&
                        movimientoOriginal.productoId == compra.productoId &&
                        movimientoOriginal.cantidad > 0.0
            ) {
                "El movimiento de inventario original es inválido"
            }

            val existencia = inventarioDao.existencia(compra.productoId)
            require(existencia + 0.000001 >= movimientoOriginal.cantidad) {
                "Inventario insuficiente para anular. Existencia: $existencia kg"
            }

            inventarioDao.insertMov(
                KardexMovimientoEntity(
                    id = UUID.randomUUID().toString(),
                    fecha = System.currentTimeMillis(),
                    productoId = compra.productoId,
                    tipo = "SALIDA",
                    cantidad = movimientoOriginal.cantidad,
                    costoUnit = movimientoOriginal.costoUnit,
                    docTipo = "ANULACION_COMPRA",
                    docId = id.toString(),
                    nota = "Anulación de compra $id"
                )
            )

            if (cuenta != null) {
                check(cuentaPorPagarDao.anularCuentaPorPagar(cuenta.id) == 1) {
                    "No fue posible anular la cuenta por pagar"
                }
            }

            check(compraDao.anularCompra(id) == 1) {
                "No fue posible anular la compra"
            }
        }
    }

    override suspend fun obtenerCompraPorId(id: Int): CompraCafeEntity? {
        return compraDao.obtenerPorId(id)
    }

    override suspend fun actualizarCompra(compra: CompraCafeEntity) {
        compraDao.actualizar(compra)
    }

    override suspend fun obtenerMovimientoKardexPorCompraId(
        compraId: Int
    ): KardexMovimientoEntity? {
        return inventarioDao.obtenerMovimientoPorDocumento(
            docTipo = "COMPRA", docId = compraId.toString()
        )
    }

    override suspend fun actualizarMovimientoKardex(
        movimiento: KardexMovimientoEntity
    ) {
        inventarioDao.actualizarMovimiento(movimiento)
    }
}
