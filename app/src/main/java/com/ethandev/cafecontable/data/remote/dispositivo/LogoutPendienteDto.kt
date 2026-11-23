
package com.ethandev.cafecontable.data.remote.dispositivo

data class LogoutPendienteDto(
    val solicitudId: String,
    val dispositivoId: String,
    val refreshToken: String
)
