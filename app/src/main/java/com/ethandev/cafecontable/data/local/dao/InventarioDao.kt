package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.InventarioResumen
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity

@Dao
interface InventarioDao {

    // Insertar movimiento en kardex
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMov(mov: KardexMovimientoEntity)

    // Obtener existencia actual de un producto
    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN tipo IN ('ENTRADA','AJUSTE') THEN cantidad
                WHEN tipo = 'SALIDA' THEN -cantidad
                ELSE 0
            END
        ), 0)
        FROM kardex_mov
        WHERE productoId = :productoId
    """)
    suspend fun existencia(productoId: String): Double

    // Historial de movimientos del producto
    @Query("""
        SELECT * FROM kardex_mov
        WHERE productoId = :productoId
        ORDER BY fecha DESC
    """)
    suspend fun historial(productoId: String): List<KardexMovimientoEntity>

    // Inventario total por producto (para reportes)
    @Query("""
        SELECT productoId,
        COALESCE(SUM(
            CASE
                WHEN tipo IN ('ENTRADA','AJUSTE') THEN cantidad
                WHEN tipo = 'SALIDA' THEN -cantidad
                ELSE 0
            END
        ),0) as existencia
        FROM kardex_mov
        GROUP BY productoId
    """)
    suspend fun inventarioGeneral(): List<InventarioResumen>
}