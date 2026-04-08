package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.MezclaHistorialItem
import com.ethandev.cafecontable.domain.model.RegistrarMezclaInput

interface MezclaRepository {
    suspend fun registrarMezcla(input: RegistrarMezclaInput)
    suspend fun obtenerHistorialMezclas(): List<MezclaHistorialItem>
    suspend fun actualizarEstadoMezcla( mezclaId: String,estado: String): Int

    suspend fun marcarPendienteEntrega(
        mezclaId: String,
        numeroSacosEnviados: Int,
        kilajeEnviado: Double
    ): Int

    suspend fun marcarEntregado(
        mezclaId: String,
        numeroSacosEntregados: Int,
        kilajeEntregado: Double,
        lugarEntrega: String
    ): Int

    suspend fun marcarAnalizado(
        mezclaId: String,
        factorRendimiento: Double
    ): Int

}