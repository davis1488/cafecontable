package com.ethandev.cafecontable.data.remote.dispositivo

/** Datos necesarios para recuperar una renovacion ya enviada al servidor. */
data class RenovacionPendienteDto(
    val solicitudId: String,
    val dispositivoId: String,
    val refreshTokenAnterior: String
)
