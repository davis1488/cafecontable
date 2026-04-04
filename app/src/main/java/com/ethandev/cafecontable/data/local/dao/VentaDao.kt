package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.VentaCafeEntity
import com.ethandev.cafecontable.data.local.entity.VentaHistorialRow

@Dao
interface VentaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(venta: VentaCafeEntity)

    @Query("""
        SELECT * FROM venta_cafe
        WHERE id = :ventaId
        LIMIT 1
    """)
    suspend fun obtenerPorId(ventaId: String): VentaCafeEntity?

    @Query("""
        SELECT * FROM venta_cafe
        ORDER BY fecha DESC
        LIMIT :limit
    """)
    suspend fun ultimas(limit: Int = 30): List<VentaCafeEntity>

    @Query("""
        SELECT
            v.id AS id,
            v.fecha AS fecha,
            p.nombre AS productoNombre,
            p.unidad AS unidad,
            v.cantidad AS cantidad,
            v.precioUnitVenta AS precioUnitVenta,
            v.cliente AS cliente,
            v.esCredito AS esCredito,
            v.nota AS nota,
            v.factor AS factor
        FROM venta_cafe v
        INNER JOIN producto p ON p.id = v.productoId
        ORDER BY v.fecha DESC
    """)
    suspend fun historial(): List<VentaHistorialRow>
}