package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.OperacionDao
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.constants.TipoOperacion
import com.ethandev.cafecontable.domain.repository.OperacionRepository
import com.ethandev.cafecontable.domain.model.GastoOperacionModel
import com.ethandev.cafecontable.domain.model.OperacionResumenModel


class OperacionRepositoryImpl(
    private val db: AppDatabase
) : OperacionRepository {

    override suspend fun listarPorTipo(
        tipo: String
    ): List<OperacionEntity> {
        return db.operacionDao().listarPorTipo(tipo)
    }

    override suspend fun listarOperaciones(): List<OperacionResumenModel> {

        val tipos = listOf(
            TipoOperacion.COMPRA,
            TipoOperacion.MEZCLA,
            TipoOperacion.DESPACHO
        )

        val resultado = mutableListOf<OperacionResumenModel>()

        tipos.forEach { tipo ->
            val lista = db.operacionDao().listarPorTipo(tipo)

            lista.forEach { operacion ->
                val total = db.gastoOperacionDao()
                    .obtenerTotalGastosPorOperacion(operacion.id)

                resultado.add(
                    OperacionResumenModel(
                        id = operacion.id,
                        nombre = operacion.nombre,
                        tipo = operacion.tipo,
                        descripcion = operacion.descripcion,
                        totalGastos = total
                    )
                )
            }
        }

        return resultado
    }
}