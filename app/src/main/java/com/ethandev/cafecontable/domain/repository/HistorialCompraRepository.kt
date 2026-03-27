package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.domain.model.CompraHistorialItem

interface HistorialCompraRepository {
    suspend fun obtenerHistorialCompras(): List<CompraHistorialItem>

    suspend fun anularCompra(id: Int)

    suspend fun obtenerCompraPorId(id: Int): CompraCafeEntity?

    suspend fun actualizarCompra(compra: CompraCafeEntity)

    suspend fun obtenerMovimientoKardexPorCompraId(compraId: Int): KardexMovimientoEntity?

    suspend fun actualizarMovimientoKardex(movimiento: KardexMovimientoEntity)
}