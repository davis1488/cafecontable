package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.MezclaDespachoEntity

@Dao
interface MezclaDespachoDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(item: MezclaDespachoEntity)

    @Query("SELECT * FROM mezcla_despacho WHERE mezclaId = :mezclaId")
    suspend fun listarPorMezcla(mezclaId: String): List<MezclaDespachoEntity>

    @Query("SELECT COUNT(*) FROM mezcla_despacho WHERE mezclaId=:mezclaId AND operacionDespachoId=:operacionId")
    suspend fun existe(mezclaId: String, operacionId: String): Int

    /** Porción de los gastos de cada despacho compartido que corresponde a esta mezcla. */
    @Query("""
        SELECT COALESCE(SUM(
            md.kgVinculados *
            (SELECT COALESCE(SUM(g.valor),0) * 1.0 FROM gasto_operacion g
             WHERE g.operacionId = md.operacionDespachoId AND g.estado = 'ACTIVO') /
            NULLIF((SELECT SUM(md2.kgVinculados) FROM mezcla_despacho md2
                    WHERE md2.operacionDespachoId = md.operacionDespachoId),0)
        ),0)
        FROM mezcla_despacho md WHERE md.mezclaId = :mezclaId
    """)
    suspend fun gastosDespachoPorMezcla(mezclaId: String): Double
}
