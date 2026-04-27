package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.UtilidadOperacionEntity
import com.ethandev.cafecontable.domain.model.UtilidadOperacionModel
import com.ethandev.cafecontable.domain.model.UtilidadPendienteModel

@Dao
interface UtilidadOperacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entity: UtilidadOperacionEntity)

//    @Query("""
//        SELECT
//            le.id AS liquidacionId,
//            le.mezclaId AS mezclaId,
//            le.pedidoId AS pedidoId,
//            vp.cliente AS cliente,
//            le.cantidadKg AS cantidadKg,
//            le.valorBase AS valorVentaBruto,
//            le.ajusteFactor AS ajusteFactor,
//            (le.descuentoCooperativa + le.otrosDescuentos) AS descuentos,
//            le.valorNeto AS valorVentaNeto,
//            m.costoPromedioKg AS costoPromedioKg
//        FROM liquidacion_entrega le
//        INNER JOIN mezcla m ON m.id = le.mezclaId
//        INNER JOIN venta_pedido vp ON vp.id = le.pedidoId
//        WHERE NOT EXISTS (
//            SELECT 1
//            FROM utilidad_operacion u
//            WHERE u.liquidacionId = le.id
//        )
//        ORDER BY le.fecha DESC
//    """)
//    suspend fun listarPendientes(): List<UtilidadPendienteModel>

    @Query("""
    SELECT 
        le.id AS liquidacionId,
        le.mezclaId AS mezclaId,
        le.pedidoId AS pedidoId,

        NULL AS operacionCompraId,
        m.operacionMezclaId AS operacionMezclaId,
        NULL AS operacionEntregaId,

        le.cliente AS cliente,
        le.cantidadKg AS cantidadKg,
        le.valorBase AS valorVentaBruto,
        le.ajusteFactor AS ajusteFactor,
        (le.descuentoCooperativa + le.otrosDescuentos) AS descuentos,
        le.valorNeto AS valorVentaNeto,
        m.costoPromedioKg AS costoPromedioKg,

        0 AS gastosCompra,
        0 AS gastosMezcla,
        0 AS gastosEntrega,
        0 AS totalGastos

    FROM liquidacion_entrega le
    INNER JOIN mezcla m ON m.id = le.mezclaId
    WHERE NOT EXISTS (
        SELECT 1 
        FROM utilidad_operacion u 
        WHERE u.liquidacionId = le.id
    )
    ORDER BY le.fecha DESC
""")
    suspend fun listarPendientes(): List<UtilidadPendienteModel>
    @Query("""
        SELECT 
            u.id AS id,
            u.fecha AS fecha,
            u.liquidacionId AS liquidacionId,
            u.mezclaId AS mezclaId,
            u.pedidoId AS pedidoId,
            vp.cliente AS cliente,
            u.cantidadKg AS cantidadKg,
            u.valorVentaNeto AS valorVentaNeto,
            u.costoCafe AS costoCafe,
            u.totalGastos AS totalGastos,
            u.utilidadBruta AS utilidadBruta,
            u.utilidadNeta AS utilidadNeta,
            u.margenPorcentaje AS margenPorcentaje,
            u.nota AS nota
        FROM utilidad_operacion u
        INNER JOIN venta_pedido vp ON vp.id = u.pedidoId
        ORDER BY u.fecha DESC
    """)
    suspend fun listarHistorial(): List<UtilidadOperacionModel>

    @Query("""
        SELECT COALESCE(SUM(valorVentaNeto), 0)
        FROM utilidad_operacion
    """)
    suspend fun totalVentasNetas(): Long

    @Query("""
        SELECT COALESCE(SUM(costoCafe), 0)
        FROM utilidad_operacion
    """)
    suspend fun totalCostoCafe(): Long

    @Query("""
        SELECT COALESCE(SUM(totalGastos), 0)
        FROM utilidad_operacion
    """)
    suspend fun totalGastos(): Long

    @Query("""
        SELECT COALESCE(SUM(utilidadNeta), 0)
        FROM utilidad_operacion
    """)
    suspend fun totalUtilidadNeta(): Long
}