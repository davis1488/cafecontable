package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.VentaDao
import com.ethandev.cafecontable.domain.model.VentaHistorialItem
import com.ethandev.cafecontable.domain.repository.HistorialVentaRepository

class HistorialVentaRepositoryImpl(
    private val dao: VentaDao
) : HistorialVentaRepository {

    override suspend fun obtenerHistorialVentas(): List<VentaHistorialItem> {
        return dao.historial().map {
            VentaHistorialItem(
                id = it.id,
                fecha = it.fecha,
                productoNombre = it.productoNombre,
                unidad = it.unidad,
                cantidad = it.cantidad,
                precioUnitVenta = it.precioUnitVenta,
                cliente = it.cliente,
                esCredito = it.esCredito,
                nota = it.nota,
                factor = it.factor
            )
        }
    }
}