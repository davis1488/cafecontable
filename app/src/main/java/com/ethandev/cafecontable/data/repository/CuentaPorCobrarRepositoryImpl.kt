package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.CuentaPorCobrarDao
import com.ethandev.cafecontable.domain.model.CuentaPorCobrarModel
import com.ethandev.cafecontable.domain.repository.CuentaPorCobrarRepository

class CuentaPorCobrarRepositoryImpl(
    private val dao: CuentaPorCobrarDao
) : CuentaPorCobrarRepository {

    override suspend fun listarPendientes(): List<CuentaPorCobrarModel> {
        return dao.listarPendientes().map {
            CuentaPorCobrarModel(
                id = it.id,
                fecha = it.fecha,
                ventaId = it.ventaId,
                cliente = it.cliente,
                valorInicial = it.valorInicial,
                saldoPendiente = it.saldoPendiente,
                estado = it.estado,
                nota = it.nota
            )
        }
    }
}