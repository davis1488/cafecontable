
package com.ethandev.cafecontable.data.remote.dispositivo

data class CerrarSesionRequestDto(
    val refreshToken: String,
    val dispositivoId: String
)
