

package com.ethandev.cafecontable.data.remote.dispositivo

data class DesafioDispositivoResponseDto(
    val desafioId: String,
    val desafio: String
)

data class RegistrarDispositivoRequestDto(
    val desafioId: String,
    val desafio: String,
    val clavePublica: String,
    val firma: String,
    val nombreDispositivo: String
)

data class RegistrarDispositivoResponseDto(
    val dispositivoId: String
)

data class VincularSesionDispositivoRequestDto(
    val dispositivoId: String,
    val desafioId: String,
    val desafio: String,
    val refreshToken: String,
    val firma: String
)

data class RefreshFirmadoRequestDto(
    val refreshToken: String,
    val solicitudId: String,
    val dispositivoId: String,
    val desafioId: String,
    val desafio: String,
    val firma: String
)


data class RecuperarRefreshFirmadoRequestDto(
    val refreshToken: String,
    val solicitudId: String,
    val dispositivoId: String,
    val desafioId: String,
    val desafio: String,
    val firma: String
)


data class SolicitarDesafioRenovacionDto(
    val refreshToken: String,
    val dispositivoId: String
)
