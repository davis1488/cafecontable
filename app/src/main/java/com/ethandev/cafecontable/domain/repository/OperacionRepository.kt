package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.data.local.entity.OperacionEntity

interface OperacionRepository {
    suspend fun listarPorTipo(tipo: String): List<OperacionEntity>
}