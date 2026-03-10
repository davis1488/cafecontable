package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository

class HistorialCompraRepositoryImpl(
    private val dao: CompraDao
) : HistorialCompraRepository {

    override suspend fun obtenerHistorialCompras(): List<CompraHistorialItem> {
        return dao.historial().map {
            CompraHistorialItem(
                id = it.id,
                fecha = it.fecha,
                productoNombre = it.productoNombre,
                unidad = it.unidad,
                cantidad = it.cantidad,
                precioUnitCompra = it.precioUnitCompra,
                proveedor = it.proveedor,
                esCredito = it.esCredito,
                nota = it.nota
            )
        }
    }
}