package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository

class HistorialCompraRepositoryImpl(
    private val compraDao: CompraDao,
    private val inventarioDao: InventarioDao
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
               // total = it.total,
                proveedor = it.proveedor,
                esCredito = it.esCredito,
                nota = it.nota,
                estado = it.estado
            )
        }
    }

    override suspend fun anularCompra(id: Int) {
        compraDao.anularCompra(id)
    }

    override suspend fun obtenerCompraPorId(id: Int): CompraCafeEntity? {
        return compraDao.obtenerPorId(id)
    }

    override suspend fun actualizarCompra(compra: CompraCafeEntity) {
        compraDao.actualizar(compra)
    }

    override suspend fun obtenerMovimientoKardexPorCompraId(compraId: Int): KardexMovimientoEntity? {
        return inventarioDao.obtenerMovimientoPorDocumento(
            docTipo = "COMPRA",
            docId = compraId.toString()
        )
    }

    override suspend fun actualizarMovimientoKardex(movimiento: KardexMovimientoEntity) {
        inventarioDao.actualizarMovimiento(movimiento)
    }
}