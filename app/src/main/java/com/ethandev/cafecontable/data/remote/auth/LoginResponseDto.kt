
package com.ethandev.cafecontable.data.remote.auth

data class LoginResponseDto(
    val accessToken: String,
    val expiraEn: String,
    val usuarioId: String,
    val empresaId: String,
    val nombre: String,
    val rol: String,


    // Nuevos campos para renovación de sesión.
    val refreshToken: String? = null,
    val refreshTokenExpiraEn: String? = null
)
