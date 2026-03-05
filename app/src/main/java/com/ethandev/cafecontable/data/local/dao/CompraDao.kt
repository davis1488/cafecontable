package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity

@Dao
interface CompraDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(compra: CompraCafeEntity)

    @Query("SELECT * FROM compra_cafe ORDER BY fecha DESC LIMIT :limit")
    suspend fun ultimas(limit: Int = 30): List<CompraCafeEntity>
}