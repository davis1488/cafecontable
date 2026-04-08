
package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.MezclaRepository

class ActualizarEstadoMezclaUseCase(
    private val repository: MezclaRepository
) {
    suspend operator fun invoke(
        mezclaId: String,
        nuevoEstado: String
    ): Int {
        require(mezclaId.isNotBlank()) { "El id de la mezcla es obligatorio" }
        require(nuevoEstado.isNotBlank()) { "El estado es obligatorio" }

        return repository.actualizarEstadoMezcla(
            mezclaId = mezclaId,
            estado = nuevoEstado
        )
    }
}