
package com.ethandev.cafecontable.data.remote

data class LoginResponseDto(
    val accessToken: String,
    val expiraEn: String,
    val usuarioId: String,
    val empresaId: String,
    val nombre: String,
    val rol: String
)
