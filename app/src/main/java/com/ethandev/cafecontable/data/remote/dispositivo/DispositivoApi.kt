
package com.ethandev.cafecontable.data.remote.dispositivo
import com.ethandev.cafecontable.data.remote.auth.LoginResponseDto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DispositivoApi {

    @POST("api/dispositivos/desafio")
    suspend fun solicitarDesafio(
        @Header("Authorization") token: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<DesafioDispositivoResponseDto>

    @POST("api/dispositivos/registrar")
    suspend fun registrarDispositivo(
        @Header("Authorization") token: String,
        @Body request: RegistrarDispositivoRequestDto
    ): Response<RegistrarDispositivoResponseDto>

    @POST("api/dispositivos/vincular-sesion")
    suspend fun vincularSesion(
        @Header("Authorization") token: String,
        @Body request: VincularSesionDispositivoRequestDto
    ): Response<Unit>


    @POST("api/auth/desafio-renovacion")
    suspend fun solicitarDesafioRenovacion(
        @Body request: SolicitarDesafioRenovacionDto
    ): Response<DesafioDispositivoResponseDto>


    @POST("api/auth/refresh-firmado")
    suspend fun renovarSesionFirmada(
        @Body request: RefreshFirmadoRequestDto
    ): Response<LoginResponseDto>


    @POST("api/auth/refresh-firmado/recuperar")
    suspend fun recuperarRenovacionFirmada(
        @Body request: RefreshFirmadoRequestDto
    ): Response<LoginResponseDto>


    @POST("api/auth/logout")
    suspend fun cerrarSesionRemota(
        @Header("Authorization") token: String,
        @Body request: CerrarSesionRequestDto
    ): Response<Unit>


    @POST("api/auth/desafio-logout")
    suspend fun solicitarDesafioLogout(
        @Body request: SolicitarDesafioRenovacionDto
    ): Response<DesafioDispositivoResponseDto>

    @POST("api/auth/logout-firmado")
    suspend fun cerrarSesionFirmada(
        @Body request: CerrarSesionFirmadoRequestDto
    ): Response<Unit>



}
