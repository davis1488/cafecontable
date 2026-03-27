package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.InventarioItemModel

interface InventarioRepository {
    suspend fun registrarEntrada(
        productoId: String,
        ccantiad: Double,
        costoUnitario: Long,
        nota: String?
    )
    suspend fun existencia(productoId: String) : Double
    suspend fun listarInventario(): List<InventarioItemModel>
    suspend fun obtenerInventario(): List<InventarioItemModel>
    suspend fun obtenerExistencia(productoId: String): Double
}