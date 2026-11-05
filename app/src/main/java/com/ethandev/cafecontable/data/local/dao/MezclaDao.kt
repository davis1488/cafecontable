package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.MezclaDisponibleDb
import com.ethandev.cafecontable.data.local.entity.MezclaEntity
import com.ethandev.cafecontable.data.local.entity.MezclaHistorialDb

/** Saldo por mezcla para inventario propio. Las asignaciones no pertenecen al saldo propio. */
data class MezclaSaldoInventarioDb(
    val mezclaId: String,
    val nota: String?,
    val estado: String,
    val cantidadTotal: Double,
    val kilajeEntregado: Double?,
    val cantidadAsignada: Double
)

@Dao
interface MezclaDao {

    @Query("""
        SELECT m.id AS mezclaId,
               m.nota AS nota,
               m.estado AS estado,
               m.cantidadTotal AS cantidadTotal,
               m.kilajeEntregado AS kilajeEntregado,
               COALESCE((
                   SELECT SUM(a.cantidadAsignada)
                   FROM asignacion_mezcla_pedido a
                   WHERE a.mezclaId = m.id
               ), 0) AS cantidadAsignada
        FROM mezcla m
        ORDER BY m.fecha DESC
    """)
    suspend fun saldosParaInventario(): List<MezclaSaldoInventarioDb>


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMezcla(mezcla: MezclaEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDetalles(detalles: List<MezclaDetalleEntity>)

    @Query("""
        SELECT * FROM mezcla
        ORDER BY fecha DESC
    """)
    suspend fun getAll(): List<MezclaEntity>

    @Query("""
        SELECT * FROM mezcla
        WHERE id = :mezclaId
        LIMIT 1
    """)
    suspend fun getById(mezclaId: String): MezclaEntity?

//    @Query("""
//        SELECT * FROM mezcla
//        WHERE estado = 'DISPONIBLE' AND cantidadDisponible > 0
//        ORDER BY fecha DESC
//    """)
//    suspend fun getDisponibles(): List<MezclaEntity>

    @Query("""
        SELECT * FROM mezcla_detalle
        WHERE mezclaId = :mezclaId
        ORDER BY productoNombre ASC
    """)
    suspend fun getDetallesByMezclaId(mezclaId: String): List<MezclaDetalleEntity>

    // cantidadDisponible = :cantidadDisponible,

    @Query("""
        UPDATE mezcla
        SET estado = :estado
        WHERE id = :mezclaId
    """)
    suspend fun actualizarDisponibleYEstado(
        mezclaId: String,
        //cantidadDisponible: Double,
        estado: String
    )

    @Query("""
    SELECT
        m.id AS mezclaId,
        COALESCE(m.nota, 'Mezcla sin nota') AS descripcion,
        m.cantidadTotal AS cantidadTotal,
        (
            m.cantidadTotal - COALESCE(SUM(amp.cantidadAsignada), 0)
        ) AS cantidadDisponible,
        m.costoPromedioKg AS costoPromedioKg,
        m.estado AS estado
    FROM mezcla m
    LEFT JOIN asignacion_mezcla_pedido amp
        ON amp.mezclaId = m.id
    WHERE m.estado IN (:estados)
    GROUP BY
        m.id,
        m.nota,
        m.cantidadTotal,
        m.costoPromedioKg,
        m.estado,
        m.fecha
    HAVING (
        m.cantidadTotal - COALESCE(SUM(amp.cantidadAsignada), 0)
    ) > 0
    ORDER BY m.fecha DESC
""")
    suspend fun obtenerMezclasDisponiblesParaAsignacion(
        estados: List<String>
    ): List<MezclaDisponibleDb>


    @Query("""
    SELECT 
        id,
        fecha,
        cantidadTotal,
        costoTotal,
        estado,
        nota
    FROM mezcla
    ORDER BY fecha DESC
    """)
    suspend fun obtenerHistorialMezclas(): List<MezclaHistorialDb>


    @Query("""
    UPDATE mezcla
    SET estado = :estado
    WHERE id = :mezclaId
    """)
    suspend fun actualizarEstadoMezcla(
        mezclaId: String,
        estado: String
    ): Int

    @Query("""
    UPDATE mezcla
    SET 
        estado = :estado,
        numeroSacosEnviados = :numeroSacosEnviados,
        kilajeEnviado = :kilajeEnviado
    WHERE id = :mezclaId
""")
    suspend fun marcarPendienteEntrega(
        mezclaId: String,
        estado: String,
        numeroSacosEnviados: Int,
        kilajeEnviado: Double
    ): Int

    @Query("""
    UPDATE mezcla
    SET 
        estado = :estado,
        numeroSacosEntregados = :numeroSacosEntregados,
        kilajeEntregado = :kilajeEntregado,
        lugarEntrega = :lugarEntrega
    WHERE id = :mezclaId
""")
    suspend fun marcarEntregado(
        mezclaId: String,
        estado: String,
        numeroSacosEntregados: Int,
        kilajeEntregado: Double,
        lugarEntrega: String
    ): Int

    @Query("""
    UPDATE mezcla
    SET 
        estado = :estado,
        factorRendimiento = :factorRendimiento
    WHERE id = :mezclaId
""")
    suspend fun marcarAnalizado(
        mezclaId: String,
        estado: String,
        factorRendimiento: Double
    ): Int


    //////// editar mezclas

    @Query("SELECT * FROM mezcla WHERE id = :mezclaId LIMIT 1")
    suspend fun obtenerMezclaPorId(mezclaId: String): MezclaEntity?

    @Query("SELECT * FROM mezcla_detalle WHERE mezclaId = :mezclaId")
    suspend fun obtenerDetallesPorMezclaId(mezclaId: String): List<MezclaDetalleEntity>

    @Insert
    suspend fun insertarDetalleMezcla(detalle: MezclaDetalleEntity)

    @Insert
    suspend fun insertarDetallesMezcla(detalles: List<MezclaDetalleEntity>)

    @Query("""
    UPDATE mezcla
    SET cantidadTotal = :cantidadTotal,
        costoTotal = :costoTotal,
        costoPromedioKg = :costoPromedioKg
    WHERE id = :mezclaId
""")
    suspend fun actualizarTotalesMezcla(
        mezclaId: String,
        cantidadTotal: Double,
        costoTotal: Long,
        costoPromedioKg: Long
    )

    @Query("""
    SELECT COUNT(*) 
    FROM mezcla_detalle 
    WHERE mezclaId = :mezclaId AND compraId = :compraId
""")
    suspend fun existeCompraEnMezcla(
        mezclaId: String,
        compraId: String
    ): Int

    @Query("""
        SELECT COALESCE(SUM(
            md.cantidadUsada * (
                SELECT COALESCE(SUM(go.valor), 0) * 1.0
                FROM gasto_operacion go
                WHERE go.operacionId = c.operacionCompraId
                  AND go.estado = 'ACTIVO'
            ) / NULLIF((
                SELECT SUM(c2.cantidad)
                FROM compra_cafe c2
                WHERE c2.operacionCompraId = c.operacionCompraId
            ), 0)
        ), 0)
        FROM mezcla_detalle md
        INNER JOIN compra_cafe c ON c.id = md.compraId
        WHERE md.mezclaId = :mezclaId
    """)
    suspend fun totalGastosCompraProrrateadosPorMezcla(
        mezclaId: String
    ): Double

    // Distribuye los gastos de una operación de mezcla entre TODAS las mezclas
    // asociadas a esa operación, evitando imputarlos varias veces.
    @Query("""
        SELECT COALESCE(
            m.cantidadTotal * (
                SELECT COALESCE(SUM(go.valor), 0) * 1.0
                FROM gasto_operacion go
                WHERE go.operacionId = m.operacionMezclaId
                  AND go.estado = 'ACTIVO'
            ) / NULLIF((
                SELECT SUM(m2.cantidadTotal)
                FROM mezcla m2
                WHERE m2.operacionMezclaId = m.operacionMezclaId
            ), 0), 0)
        FROM mezcla m
        WHERE m.id = :mezclaId
    """)
    suspend fun totalGastosOperacionMezclaProrrateados(
        mezclaId: String
    ): Double



    @Query("""
    UPDATE mezcla_detalle
    SET cantidadUsada = :nuevaCantidad
    WHERE id = :detalleId
""")
    suspend fun actualizarCantidadDetalle(
        detalleId: String,
        nuevaCantidad: Double
    ): Int

    @Query("""
    DELETE FROM mezcla_detalle
    WHERE id = :detalleId
      AND mezclaId = :mezclaId
""")
    suspend fun eliminarDetalleMezcla(
        detalleId: String,
        mezclaId: String
    ): Int

    @Query("""
    UPDATE mezcla
    SET nota = :nota
    WHERE id = :mezclaId
      AND estado = 'CREADO'
""")
    suspend fun actualizarNotaMezcla(
        mezclaId: String,
        nota: String?
    ): Int


    @Query("""
    DELETE FROM mezcla_detalle
    WHERE mezclaId = :mezclaId
""")
    suspend fun eliminarDetallesPorMezclaId(
        mezclaId: String
    ): Int



}


