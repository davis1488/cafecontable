package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ethandev.cafecontable.data.local.entity.VentaPedidoEntity

@Dao
interface VentaPedidoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(venta: VentaPedidoEntity)

    @Update
    suspend fun update(venta: VentaPedidoEntity)

    @Query("""
        SELECT * FROM venta_pedido
        ORDER BY fecha DESC
    """)
    suspend fun getAll(): List<VentaPedidoEntity>

    @Query("""
        SELECT * FROM venta_pedido
        WHERE estado IN ('PENDIENTE_PREPARACION', 'EN_PREPARACION', 'ENTREGA_PARCIAL')
        ORDER BY fecha DESC
    """)
    suspend fun getVentasPendientes(): List<VentaPedidoEntity>

    @Query("""
        SELECT * FROM venta_pedido
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getById(id: String): VentaPedidoEntity?

    @Query("""
        UPDATE venta_pedido
        SET cantidadEntregada = :cantidadEntregada,
            estado = :estado
        WHERE id = :id
    """)
    suspend fun actualizarEntrega(
        id: String,
        cantidadEntregada: Double,
        estado: String
    )
}