package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.VentaPedido

interface VentaPedidoRepository {
    suspend fun registrarVenta(input: VentaPedidoInput)
    suspend fun listarVentas(): List<VentaPedido>
    suspend fun listarVentasPendientes(): List<VentaPedido>
    suspend fun obtenerVentaPorId(id: String): VentaPedido?
    suspend fun actualizarAsignacion( id: String, cantidadAsignada: Double, estado: String )}