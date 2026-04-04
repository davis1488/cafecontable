package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaEntity

@Dao
interface PreparacionEntregaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPreparacion(preparacion: PreparacionEntregaEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDetalles(detalles: List<PreparacionEntregaDetalleEntity>)

    @Query("""
        SELECT * FROM preparacion_entrega
        WHERE id = :preparacionId
        LIMIT 1
    """)
    suspend fun getPreparacionById(preparacionId: String): PreparacionEntregaEntity?

    @Query("""
        SELECT * FROM preparacion_entrega
        WHERE ventaId = :ventaId
        ORDER BY fecha DESC
    """)
    suspend fun getPreparacionesPorVenta(ventaId: String): List<PreparacionEntregaEntity>

    @Query("""
        SELECT * FROM preparacion_entrega_detalle
        WHERE preparacionId = :preparacionId
        ORDER BY productoNombre ASC
    """)
    suspend fun getDetallesPorPreparacion(preparacionId: String): List<PreparacionEntregaDetalleEntity>

    @Query("""
        SELECT COALESCE(SUM(cantidadPreparada), 0)
        FROM preparacion_entrega
        WHERE ventaId = :ventaId
    """)
    suspend fun getTotalPreparadoPorVenta(ventaId: String): Double
}