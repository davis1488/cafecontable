package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mezcla")
data class MezclaEntity(
    @PrimaryKey
    val id: String,

    val fecha: Long,

    val operacionMezclaId: String?,

    val cantidadTotal: Double,
    val cantidadDisponible: Double,

    val costoCafeBase: Long,
    val gastosCompraAcumulados: Long,
//    val gastosMezcla: Long,
//
    val costoTotal: Long,
    val costoPromedioKg: Long,

    val estado: String,
    val nota: String?,

    val numeroSacosEnviados: Int? = null,
    val kilajeEnviado: Double? = null,

    val numeroSacosEntregados: Int? = null,
    val kilajeEntregado: Double? = null,
    val lugarEntrega: String? = null,

    val factorRendimiento: Double? = null
)