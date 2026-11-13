
package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "operaciones_pendientes_sync",
    indices = [
        Index(value = ["empresaId", "estado"])
    ]
)
data class OperacionPendienteSyncEntity(

    @PrimaryKey
    val id: String,

    val empresaId: String,

    val usuarioId: String,

    val tipoOperacion: String,

    val referenciaLocal: String,

    val payloadJson: String,

    val estado: String = "PENDIENTE",

    val fechaCreacion: Long =
        System.currentTimeMillis(),

    val intentos: Int = 0,

    val ultimoError: String? = null
)
