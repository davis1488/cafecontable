package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.dbmodel.LiquidacionHistorialDb
import com.ethandev.cafecontable.data.local.dbmodel.LiquidacionPendienteDb
import com.ethandev.cafecontable.data.local.entity.LiquidacionEntregaEntity

@Dao
interface LiquidacionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(liquidacion: LiquidacionEntregaEntity)

    @Query("""
    SELECT
        amp.id AS asignacionId,
        amp.pedidoId AS pedidoId,
        amp.mezclaId AS mezclaId,
        vp.cliente AS cliente,
        amp.cantidadAsignada AS cantidadKg,
        COALESCE(vp.precioUnitVenta, 0) AS precioBaseKg,
        COALESCE(m.factorRendimiento, 0) AS factorReal
    FROM asignacion_mezcla_pedido amp
    INNER JOIN venta_pedido vp ON vp.id = amp.pedidoId
    INNER JOIN mezcla m ON m.id = amp.mezclaId
    WHERE NOT EXISTS (
        SELECT 1
        FROM liquidacion_entrega le
        WHERE le.asignacionId = amp.id
    )
    ORDER BY amp.fecha DESC
""")
    suspend fun obtenerPendientesLiquidacion(): List<LiquidacionPendienteDb>

    @Query("""
        SELECT
            id,
            fecha,
            asignacionId,
            pedidoId,
            mezclaId,
            cliente,
            cantidadKg,
            precioBaseKg,
            factorReal,
            valorBase,
            ajusteFactor,
            descuentoCooperativa,
            otrosDescuentos,
            valorNeto,
            nota
        FROM liquidacion_entrega
        ORDER BY fecha DESC
    """)
    suspend fun obtenerHistorialLiquidaciones(): List<LiquidacionHistorialDb>

    @Query("""
        SELECT *
        FROM liquidacion_entrega
        WHERE asignacionId = :asignacionId
        LIMIT 1
    """)
    suspend fun obtenerPorAsignacion(asignacionId: String): LiquidacionEntregaEntity?

    @Query("""
        SELECT *
        FROM liquidacion_entrega
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun obtenerPorId(id: String): LiquidacionEntregaEntity?
}