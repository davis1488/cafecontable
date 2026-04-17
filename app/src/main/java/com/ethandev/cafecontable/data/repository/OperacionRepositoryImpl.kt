package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.OperacionDao
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.repository.OperacionRepository

class OperacionRepositoryImpl(
    private val dao: OperacionDao
) : OperacionRepository {

    override suspend fun listarPorTipo(tipo: String): List<OperacionEntity> {
        return dao.listarPorTipo(tipo)
    }
}