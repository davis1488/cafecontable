
package com.ethandev.cafecontable.data.remote.dispositivo

data class CerrarSesionFirmadoRequestDto(
    val solicitudId: String,
    val dispositivoId: String,
    val desafioId: String,
    val refreshToken: String,
    val desafio: String,
    val firma: String
)
