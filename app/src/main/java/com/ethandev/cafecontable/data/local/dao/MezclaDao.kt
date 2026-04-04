package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.MezclaDisponibleDb
import com.ethandev.cafecontable.data.local.entity.MezclaEntity

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
        m.costoPromedioKg AS costoPromedioKg
    FROM mezcla m
    WHERE m.estado = 'DISPONIBLE'
    ORDER BY m.fecha DESC
""")
    suspend fun obtenerMezclasDisponiblesParaAsignacion(): List<MezclaDisponibleDb>

}