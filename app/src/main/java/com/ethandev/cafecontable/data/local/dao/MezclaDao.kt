package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.MezclaDisponibleDb
import com.ethandev.cafecontable.data.local.entity.MezclaEntity
import com.ethandev.cafecontable.data.local.entity.MezclaHistorialDb

@Dao
interface MezclaDao {

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

    @Query("""
        SELECT * FROM mezcla
        WHERE estado = 'DISPONIBLE' AND cantidadDisponible > 0
        ORDER BY fecha DESC
    """)
    suspend fun getDisponibles(): List<MezclaEntity>

    @Query("""
        SELECT * FROM mezcla_detalle
        WHERE mezclaId = :mezclaId
        ORDER BY productoNombre ASC
    """)
    suspend fun getDetallesByMezclaId(mezclaId: String): List<MezclaDetalleEntity>

    @Query("""
        UPDATE mezcla
        SET cantidadDisponible = :cantidadDisponible,
            estado = :estado
        WHERE id = :mezclaId
    """)
    suspend fun actualizarDisponibleYEstado(
        mezclaId: String,
        cantidadDisponible: Double,
        estado: String
    )

    @Query("""
    SELECT
        m.id AS mezclaId,
        COALESCE(m.nota, 'Mezcla sin nota') AS descripcion,
        m.cantidadTotal AS cantidadTotal,
        m.cantidadDisponible AS cantidadDisponible,
        m.costoPromedioKg AS costoPromedioKg,
        m.estado AS estado
    FROM mezcla m
    WHERE m.cantidadDisponible > 0
      AND m.estado IN (:estados)
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
}


