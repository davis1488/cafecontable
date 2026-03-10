package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.CompraHistorialItem

interface HistorialCompraRepository {
    suspend fun obtenerHistorialCompras(): List<CompraHistorialItem>
}