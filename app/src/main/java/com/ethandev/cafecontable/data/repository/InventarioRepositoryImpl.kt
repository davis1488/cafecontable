package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.domain.model.InventarioItemModel
import com.ethandev.cafecontable.domain.repository.InventarioRepository
import java.util.UUID

class InventarioRepositoryImpl(
    private val dao: InventarioDao
) : InventarioRepository {

    override suspend fun registrarEntrada(productoId: String, cantidad: Double, costoUnit: Long, nota: String?) {
        dao.insertMov(
            KardexMovimientoEntity(
                id = UUID.randomUUID().toString(),
                fecha = System.currentTimeMillis(),
                productoId = productoId,
                tipo = "ENTRADA",
                cantidad = cantidad,
                costoUnit = costoUnit,
                docTipo = "AJUSTE",
                nota = nota
            )
        )
    }

    override suspend fun existencia(productoId: String): Double =
        dao.existencia(productoId)

    override suspend fun listarInventario(): List<InventarioItemModel> {
        return dao.inventarioDetalle().map {
            InventarioItemModel(
                productoId = it.productoId,
                nombre = it.nombre,
                unidad = it.unidad,
                existencia = it.existencia
            )
        }
    }
}