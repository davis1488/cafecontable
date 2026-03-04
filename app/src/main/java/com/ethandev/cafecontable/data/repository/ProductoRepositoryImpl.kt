//package com.ethandev.cafecontable.data.repository
//
//class ProductoRepositoryImpl {
//}

package com.ethandev.cafecontable.data.repository

import androidx.room.RoomDatabase
import com.ethandev.cafecontable.data.local.dao.ProductoDao
import com.ethandev.cafecontable.data.local.entity.ProductoEntity
import com.ethandev.cafecontable.domain.model.Producto
import com.ethandev.cafecontable.domain.repository.ProductoRepository
import java.util.UUID

class ProductoRepositoryImpl(
    private val dao: ProductoDao
) : ProductoRepository {

    override suspend fun crear(nombre: String, unidad: String, precioVenta: Long) {
        val entity = ProductoEntity(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            unidad = unidad.trim().uppercase(),
            precioVenta = precioVenta
        )
        dao.upsert(entity)
    }

    override suspend fun listar(): List<Producto> {
        return dao.listar().map {
            Producto(
                id = it.id,
                nombre = it.nombre,
                unidad = it.unidad,
                precioVenta = it.precioVenta
            )
        }
    }
}