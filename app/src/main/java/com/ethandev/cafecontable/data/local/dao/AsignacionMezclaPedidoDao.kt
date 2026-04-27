package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.AsignacionMezclaPedidoEntity

@Dao
interface AsignacionMezclaPedidoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(asignacion: AsignacionMezclaPedidoEntity)

    @Query("""
        SELECT * FROM asignacion_mezcla_pedido
        WHERE pedidoId = :pedidoId
        ORDER BY fecha DESC
    """)
    suspend fun getByPedidoId(pedidoId: String): List<AsignacionMezclaPedidoEntity>

    @Query("""
        SELECT * FROM asignacion_mezcla_pedido
        WHERE mezclaId = :mezclaId
        ORDER BY fecha DESC
    """)
    suspend fun getByMezclaId(mezclaId: String): List<AsignacionMezclaPedidoEntity>

    @Query("""
    SELECT COALESCE(SUM(cantidadAsignada), 0)
    FROM asignacion_mezcla_pedido
    WHERE mezclaId = :mezclaId
""")
    suspend fun totalAsignadoPorMezcla(mezclaId: String): Double
}