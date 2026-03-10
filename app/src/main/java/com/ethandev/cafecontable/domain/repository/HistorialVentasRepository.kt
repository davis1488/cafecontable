package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.VentaHistorialItem

interface HistorialVentaRepository {
    suspend fun obtenerHistorialVentas(): List<VentaHistorialItem>
}