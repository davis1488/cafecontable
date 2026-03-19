
package com.ethandev.cafecontable.domain.model

data class PrestamoModel(
    val id: Int = 0,
    val nombrePersona: String,
    val cedulaPersona: String,
    val fechaPrestamo: Long,
    val valorPrestado: Double,
    val saldoPendiente: Double,
    val interes: Double = 0.0,
    val observacion: String = "",
    val fechaVencimiento: Long? = null,
    val estado: String = "PENDIENTE"
)
