
package com.ethandev.cafecontable.data.local.entity

data class MezclaHistorialDb(
    val id: String,
    val fecha: Long,
    val cantidadTotal: Double,
    val costoTotal: Long,
    val estado: String,
    val nota: String?
)