package com.ethandev.cafecontable.domain.model

data class CrearOperacionInput(
    val tipo: String,
    val nombre: String,
    val descripcion: String?
)