package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.MezclaRepository

class MarcarMezclaPendienteEntregaUseCase(
    private val repository: MezclaRepository
) {
    suspend operator fun invoke(
        mezclaId: String,
        numeroSacosEnviados: Int,
        kilajeEnviado: Double
    ): Int {
        require(mezclaId.isNotBlank()) { "La mezcla es obligatoria" }
        require(numeroSacosEnviados > 0) { "El número de sacos debe ser mayor a cero" }
        require(kilajeEnviado > 0) { "El kilaje enviado debe ser mayor a cero" }

        return repository.marcarPendienteEntrega(
            mezclaId = mezclaId,
            numeroSacosEnviados = numeroSacosEnviados,
            kilajeEnviado = kilajeEnviado
        )
    }
}