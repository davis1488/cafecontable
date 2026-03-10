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

    @Query("""
        SELECT
            c.id AS id,
            c.fecha AS fecha,
            p.nombre AS productoNombre,
            p.unidad AS unidad,
            c.cantidad AS cantidad,
            c.precioUnitCompra AS precioUnitCompra,
            c.proveedor AS proveedor,
            c.esCredito AS esCredito,
            c.nota AS nota
        FROM compra_cafe c
        INNER JOIN producto p ON p.id = c.productoId
        ORDER BY c.fecha DESC
    """)
    suspend fun historial(): List<com.ethandev.cafecontable.data.local.entity.CompraHistorialRow>

}