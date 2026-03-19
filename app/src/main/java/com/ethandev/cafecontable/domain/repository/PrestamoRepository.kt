
package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.AbonoPrestamoModel
import com.ethandev.cafecontable.domain.model.PrestamoModel
import kotlinx.coroutines.flow.Flow

interface PrestamoRepository {
    suspend fun registrarPrestamo(prestamo: PrestamoModel)
    suspend fun registrarAbono(abono: AbonoPrestamoModel)
    fun listarPrestamos(): Flow<List<PrestamoModel>>
    fun buscarPrestamos(query: String): Flow<List<PrestamoModel>>
    fun listarAbonos(prestamoId: Int): Flow<List<AbonoPrestamoModel>>
    suspend fun obtenerPrestamoPorId(id: Int): PrestamoModel?
    suspend fun actualizarPrestamo(prestamo: PrestamoModel)
    fun totalPrestadoPendiente(): Flow<Double?>
}