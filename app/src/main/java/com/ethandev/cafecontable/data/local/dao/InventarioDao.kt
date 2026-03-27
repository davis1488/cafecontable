package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ethandev.cafecontable.data.local.entity.InventarioItem
import com.ethandev.cafecontable.data.local.entity.InventarioResumen
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity

@Dao
interface InventarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMov(mov: KardexMovimientoEntity)

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

    @Query("""
        SELECT * FROM kardex_mov
        WHERE productoId = :productoId
        ORDER BY fecha DESC
    """)
    suspend fun historial(productoId: String): List<KardexMovimientoEntity>

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

    @Query("""
        SELECT 
            p.id AS productoId,
            p.nombre AS nombre,
            p.unidad AS unidad,
            COALESCE(SUM(
                CASE
                    WHEN k.tipo IN ('ENTRADA','AJUSTE') THEN k.cantidad
                    WHEN k.tipo = 'SALIDA' THEN -k.cantidad
                    ELSE 0
                END
            ), 0) AS existencia
        FROM producto p
        LEFT JOIN kardex_mov k ON k.productoId = p.id
        WHERE p.activo = 1
        GROUP BY p.id, p.nombre, p.unidad
        ORDER BY p.nombre
    """)
    suspend fun inventarioDetalle(): List<InventarioItem>

    @Query("""
        SELECT * FROM kardex_mov
        WHERE docTipo = :docTipo AND docId = :docId
        LIMIT 1
    """)
    suspend fun obtenerMovimientoPorDocumento(
        docTipo: String,
        docId: String
    ): KardexMovimientoEntity?

    @Update
    suspend fun actualizarMovimiento(movimiento: KardexMovimientoEntity)
}