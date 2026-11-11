package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity

/** Relación explícita: una operación de despacho puede transportar varias mezclas. */
@Entity(tableName = "mezcla_despacho", primaryKeys = ["mezclaId", "operacionDespachoId"])
data class MezclaDespachoEntity(
    val mezclaId: String,
    val operacionDespachoId: String,
    val kgVinculados: Double,
    val fechaVinculacion: Long
)
