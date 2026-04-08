package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.MezclaRepository

class MarcarMezclaEntregadoUseCase(
    private val repository: MezclaRepository
) {
    suspend operator fun invoke(
        mezclaId: String,
        numeroSacosEntregados: Int,
        kilajeEntregado: Double,
        lugarEntrega: String
    ): Int {
        return repository.marcarEntregado(
            mezclaId,
            numeroSacosEntregados,
            kilajeEntregado,
            lugarEntrega
        )
    }
}