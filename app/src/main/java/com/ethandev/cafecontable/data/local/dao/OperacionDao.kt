package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.OperacionEntity

@Dao
interface OperacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operacion: OperacionEntity)

    @Query("SELECT * FROM operacion WHERE tipo = :tipo AND estado = 'ACTIVA' ORDER BY fecha DESC")
    suspend fun listarPorTipo(tipo: String): List<OperacionEntity>

    @Query("SELECT * FROM operacion WHERE id = :id")
    suspend fun obtenerPorId(id: String): OperacionEntity?
}