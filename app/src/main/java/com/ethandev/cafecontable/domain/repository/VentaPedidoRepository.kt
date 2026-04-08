//package com.ethandev.cafecontable.domain.repository
//
//import com.ethandev.cafecontable.domain.model.VentaPedido
//
//interface VentaPedidoRepository {
//    suspend fun registrarVenta(input: VentaPedidoInput)
//    suspend fun listarVentas(): List<VentaPedido>
//    suspend fun listarVentasPendientes(): List<VentaPedido>
//    suspend fun obtenerVentaPorId(id: String): VentaPedido?
//    suspend fun actualizarAsignacion( id: String, cantidadAsignada: Double, estado: String )}

package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.VentaPedido

interface VentaPedidoRepository {
    suspend fun registrarVenta(input: VentaPedidoInput)
    suspend fun listarVentas(): List<VentaPedido>
    suspend fun listarVentasPendientes(): List<VentaPedido>
    suspend fun obtenerVentaPorId(id: String): VentaPedido?
    suspend fun actualizarAsignacion(
        id: String,
        cantidadAsignada: Double,
        estado: String
    )

    suspend fun marcarComoEntregado(id: String)
    suspend fun marcarComoAnalizado(id: String)
    suspend fun finalizarVenta(id: String)
    suspend fun actualizarEstado (
        pedidoId: String,
        estado: String
    ): Int

    suspend fun obtenerPorId (id: String):VentaPedido?

    suspend fun actualizarEntrega(
        pedidoId: String,
        estado: String,
        numeroSacos: Int,
        pesoNeto: Double,
        pesoBruto: Double
    ): Int

    suspend fun actualizarAnalisis(
        pedidoId: String,
        estado: String,
        factorAnalisis: Double,
        ajusteAnalisis: Double
    ): Int

}