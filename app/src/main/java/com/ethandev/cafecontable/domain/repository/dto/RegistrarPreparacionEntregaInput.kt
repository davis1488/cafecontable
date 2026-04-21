package com.ethandev.cafecontable.domain.repository.dto

data class RegistrarPreparacionEntregaInput(
    val ventaId: String,
    val cantidadCafe: Double,
    val cantidadPasilla: Double,
    val cantidadRegular: Double,
    val nota: String?
)