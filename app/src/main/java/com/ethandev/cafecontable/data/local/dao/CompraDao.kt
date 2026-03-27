package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.CompraHistorialRow

@Dao
interface CompraDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(compra: CompraCafeEntity): Long

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
            (c.cantidad * c.precioUnitCompra) AS total,
            c.proveedor AS proveedor,
            c.esCredito AS esCredito,
            c.nota AS nota,
            c.estado AS estado
        FROM compra_cafe c
        INNER JOIN producto p ON p.id = c.productoId
        ORDER BY c.fecha DESC
    """)
    suspend fun historial(): List<CompraHistorialRow>

    @Query("""
        UPDATE compra_cafe
        SET estado = 'ANULADA'
        WHERE id = :id
    """)
    suspend fun anularCompra(id: Int)

    @Query("""
        SELECT * FROM compra_cafe
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun obtenerPorId(id: Int): CompraCafeEntity?

    @Update
    suspend fun actualizar(compra: CompraCafeEntity)
}