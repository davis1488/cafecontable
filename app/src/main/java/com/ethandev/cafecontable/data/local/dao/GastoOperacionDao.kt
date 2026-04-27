package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.GastoOperacionEntity

@Dao
interface GastoOperacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gasto: GastoOperacionEntity)

    @Query("""
        SELECT COALESCE(SUM(valor), 0)
        FROM gasto_operacion
        WHERE operacionId = :operacionId AND estado = 'ACTIVO'
    """)
    suspend fun totalPorOperacion(operacionId: String): Long

    @Query("""
    SELECT COALESCE(SUM(valor), 0)
    FROM gasto_operacion
    WHERE operacionId = :operacionId
      AND estado = 'ACTIVO'
""")
    suspend fun obtenerTotalGastosPorOperacion(operacionId: String): Long

    @Query("""
    SELECT * FROM gasto_operacion
    WHERE operacionId = :operacionId
    ORDER BY fecha DESC
""")
    suspend fun listarPorOperacion(operacionId: String): List<GastoOperacionEntity>
}