
package com.ethandev.cafecontable.data.remote.compra

data class CompraApiDto(
    val id: Int,
    val fecha: String,
    val cantidadKg: Double,
    val precioPorKg: Double,
    val estado: Int,
    val empresaId: String?
)
