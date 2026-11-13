
package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.OperacionPendienteSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperacionPendienteSyncDao {

    @Insert
    suspend fun insertar(
        operacion: OperacionPendienteSyncEntity
    )

    @Query("""
        SELECT * FROM operaciones_pendientes_sync
        WHERE empresaId = :empresaId
        AND estado = 'PENDIENTE'
        ORDER BY fechaCreacion ASC
    """)
    suspend fun obtenerPendientes(
        empresaId: String
    ): List<OperacionPendienteSyncEntity>

    @Query("""
        SELECT * FROM operaciones_pendientes_sync
        WHERE empresaId = :empresaId
        ORDER BY fechaCreacion DESC
    """)
    fun observarOperaciones(
        empresaId: String
    ): Flow<List<OperacionPendienteSyncEntity>>

    @Query("""
        UPDATE operaciones_pendientes_sync
        SET estado = :estado,
            ultimoError = :error
        WHERE id = :id
    """)
    suspend fun actualizarEstado(
        id: String,
        estado: String,
        error: String? = null
    )

    @Query("""
        UPDATE operaciones_pendientes_sync
        SET intentos = intentos + 1
        WHERE id = :id
    """)
    suspend fun incrementarIntentos(
        id: String
    )


    @Query("""
    UPDATE operaciones_pendientes_sync
    SET payloadJson = :payloadJson,
        ultimoError = NULL
    WHERE id = :id
      AND estado = 'PENDIENTE'
""")
    suspend fun actualizarPayload(
        id: String,
        payloadJson: String
    )


}
