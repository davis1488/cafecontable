package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.AbonoPrestamoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AbonoPrestamoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAbono(abono: AbonoPrestamoEntity)

    @Query("""
        SELECT * FROM abonos_prestamo
        WHERE prestamoId = :prestamoId
        ORDER BY fechaAbono DESC
    """)
    fun listarAbonosPorPrestamo(prestamoId: Int): Flow<List<AbonoPrestamoEntity>>
}