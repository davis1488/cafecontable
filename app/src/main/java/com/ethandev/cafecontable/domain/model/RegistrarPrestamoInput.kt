
package com.ethandev.cafecontable.domain.model

data class RegistrarPrestamoInput(
    val nombrePersona: String,
    val cedulaPersona: String,
    val fechaPrestamo: Long,
    val valorPrestado: Double,
    val interes: Double = 0.0,
    val observacion: String = "",
    val fechaVencimiento: Long? = null
)