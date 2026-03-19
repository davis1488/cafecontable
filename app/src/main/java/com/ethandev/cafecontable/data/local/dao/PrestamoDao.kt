package com.ethandev.cafecontable.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ethandev.cafecontable.data.entity.PrestamoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrestamoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPrestamo(prestamo: PrestamoEntity): Long

    @Update
    suspend fun actualizarPrestamo(prestamo: PrestamoEntity)

    @Query("SELECT * FROM prestamos ORDER BY fechaPrestamo DESC")
    fun listarPrestamos(): Flow<List<PrestamoEntity>>

    @Query("SELECT * FROM prestamos WHERE id = :id LIMIT 1")
    suspend fun obtenerPrestamoPorId(id: Int): PrestamoEntity?

    @Query("""
        SELECT * FROM prestamos
        WHERE nombrePersona LIKE '%' || :query || '%'
           OR cedulaPersona LIKE '%' || :query || '%'
        ORDER BY fechaPrestamo DESC
    """)
    fun buscarPrestamos(query: String): Flow<List<PrestamoEntity>>

    @Query("SELECT SUM(saldoPendiente) FROM prestamos WHERE estado = 'PENDIENTE'")
    fun totalPrestadoPendiente(): Flow<Double?>
}