package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.CuentaPorCobrarEntity

@Dao
interface CuentaPorCobrarDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(cuenta: CuentaPorCobrarEntity)

    @Query("""
        SELECT * FROM cuenta_por_cobrar
        ORDER BY fecha DESC
    """)
    suspend fun listarTodas(): List<CuentaPorCobrarEntity>

    @Query("""
        SELECT * FROM cuenta_por_cobrar
        WHERE estado = 'PENDIENTE'
        ORDER BY fecha DESC
    """)
    suspend fun listarPendientes(): List<CuentaPorCobrarEntity>
}