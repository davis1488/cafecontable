package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ethandev.cafecontable.data.local.entity.PedidoDisponibleDb
import com.ethandev.cafecontable.data.local.entity.VentaPedidoEntity

@Dao
interface VentaPedidoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pedido: VentaPedidoEntity)

    @Update
    suspend fun update(pedido: VentaPedidoEntity)

    @Query("""
        SELECT * FROM venta_pedido
        ORDER BY fecha DESC
    """)
    suspend fun getAll(): List<VentaPedidoEntity>

    @Query("""
        SELECT * FROM venta_pedido
        WHERE estado IN ('PENDIENTE_ASIGNACION', 'ASIGNACION_PARCIAL')
        ORDER BY fecha DESC
    """)
    suspend fun getPendientes(): List<VentaPedidoEntity>

//    @Query("""
//        SELECT * FROM venta_pedido
//        WHERE id = :pedidoId
//        LIMIT 1
//    """)
//    suspend fun obtenerPorId(pedidoId: String): VentaPedidoEntity?

//    @Query("""
//        UPDATE venta_pedido
//        SET cantidadAsignada = :cantidadAsignada,
//            estado = :estado
//        WHERE id = :pedidoId
//    """)
//    suspend fun actualizarAsignacionYEstado(
//        pedidoId: String,
//        cantidadAsignada: Double,
//        estado: String
//    )

    @Query("""
    UPDATE venta_pedido
    SET cantidadAsignada = :cantidadAsignada,
        estado = :estado
    WHERE id = :pedidoId
""")
    suspend fun actualizarEntregaYEstado(
        pedidoId: String,
        cantidadAsignada: Double,
        estado: String
    )

    @Query("""
    SELECT
        vp.id AS pedidoId,
        vp.cliente AS clienteNombre,
        'Producto' AS productoNombre,
        vp.cantidadPactada AS cantidadPedido,
        vp.cantidadAsignada AS cantidadAsignada,
        vp.precioUnitVenta AS precioUnitVenta
    FROM venta_pedido vp
    WHERE vp.estado = 'CREADO' OR vp.estado  = 'ASIGNACION_PARCIAL'
    ORDER BY vp.fecha DESC
""")
    suspend fun obtenerPedidosDisponiblesParaAsignacion(): List<PedidoDisponibleDb>



    @Query("""
    SELECT * 
    FROM venta_pedido
    WHERE id = :pedidoId
    LIMIT 1
""")
    suspend fun obtenerPorId(pedidoId: String): VentaPedidoEntity?



    @Query("""
    UPDATE venta_pedido
    SET
        estado = :estado,
        factorAnalisis = :factorAnalisis,
        ajusteAnalisis = :ajusteAnalisis
    WHERE id = :pedidoId
""")
    suspend fun actualizarAnalisis(
        pedidoId: String,
        estado: String,
        factorAnalisis: Double,
        ajusteAnalisis: Long
    ): Int

    @Query("""
    UPDATE venta_pedido
    SET
        estado = :estado
    WHERE id = :pedidoId
""")
    suspend fun actualizarEstado(
        pedidoId: String,
        estado: String
    ): Int





    @Query("""
        UPDATE venta_pedido
        SET estado = 'ENTREGADO'
        WHERE id = :pedidoId
    """)
    suspend fun marcarComoEntregado(pedidoId: String)

    @Query("""
        UPDATE venta_pedido
        SET estado = 'ANALIZADO'
        WHERE id = :pedidoId
    """)
    suspend fun marcarComoAnalizado(pedidoId: String)

    @Query("""
        UPDATE venta_pedido
        SET estado = 'FINALIZADO'
        WHERE id = :pedidoId
    """)
    suspend fun finalizarVenta(pedidoId: String)


    @Query("""
    UPDATE venta_pedido
    SET 
        estado = :estado,
        numeroSacos = :numeroSacos,
        pesoNeto = :pesoNeto,
        pesoBruto = :pesoBruto
    WHERE id = :pedidoId
""")
    suspend fun actualizarEntrega(
        pedidoId: String,
        estado: String,
        numeroSacos: Int,
        pesoNeto: Double,
        pesoBruto: Double
    ): Int

//    @Query("""
//    UPDATE venta_pedido
//    SET estado = :estado
//    WHERE id = :pedidoId
//""")
//    suspend fun actualizarEstado(pedidoId: String, estado: String)

}