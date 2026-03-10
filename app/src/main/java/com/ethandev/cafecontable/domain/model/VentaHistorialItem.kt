package com.ethandev.cafecontable.domain.model

data class VentaHistorialItem(
    val id: String,
    val fecha: Long,
    val productoNombre: String,
    val unidad: String,
    val cantidad: Double,
    val precioUnitVenta: Long,
    val factor: Double = 90.0,
    val cliente: String?,
    val esCredito: Boolean,
    val nota: String?
) {
    val precioFinal: Double
        get(){
            val diferencia = factor - 90
            return precioUnitVenta * (1- diferencia/ 100)
        }
    val total: Long
        get() = (cantidad * precioFinal).toLong()
}