package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.CuentaPorCobrarModel

interface CuentaPorCobrarRepository {
    suspend fun listarPendientes():List<CuentaPorCobrarModel>
}