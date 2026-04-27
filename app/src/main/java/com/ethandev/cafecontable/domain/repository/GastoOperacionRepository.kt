package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.GastoOperacionModel

interface GastoOperacionRepository {
    suspend fun listarGastosPorOperacion(
        operacionId: String
    ): List<GastoOperacionModel>
}