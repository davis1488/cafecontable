package com.ethandev.cafecontable.domain.repository

data class VentaCafeInput(
    val productoNombre: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitVenta: Long,
    val cliente: String?,
    val esCredito: Boolean,
    val nota: String?,
    val factor: Double
)

interface VentaRepository {
    suspend fun registrarVenta(input: VentaCafeInput)
}