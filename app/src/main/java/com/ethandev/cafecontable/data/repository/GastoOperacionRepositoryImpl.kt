package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.domain.model.GastoOperacionModel
import com.ethandev.cafecontable.domain.repository.GastoOperacionRepository

class GastoOperacionRepositoryImpl(
    private val db: AppDatabase
) : GastoOperacionRepository {

    override suspend fun listarGastosPorOperacion(
        operacionId: String
    ): List<GastoOperacionModel> {

        return db.gastoOperacionDao()
            .listarPorOperacion(operacionId)
            .map {
                GastoOperacionModel(
                    id = it.id,
                    operacionId = it.operacionId,
                    categoria = it.categoria,
                    descripcion = it.descripcion,
                    valor = it.valor,
                    tercero = it.tercero,
                    observacion = it.observacion,
                    fecha = it.fecha
                )
            }
    }
}