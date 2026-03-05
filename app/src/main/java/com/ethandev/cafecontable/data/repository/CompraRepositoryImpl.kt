package com.ethandev.cafecontable.data.repository

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.ProductoEntity
//import com.ethandev.cafecontable.domain.model.CompraCafeImput
import com.ethandev.cafecontable.domain.repository.CompraRepository
import com.ethandev.cafecontable.domain.repository.CompraCafeImput

import java.util.UUID

class CompraRepositoryImpl(
    private val db: AppDatabase
) : CompraRepository {

    override suspend fun registrarCompra(input: CompraCafeImput) {
        db.withTransaction {

            val productoDao = db.productoDao()
            val inventarioDao = db.inventarioDao()
            val compraDao = db.compraDao()

            // 1) getOrCreate Producto (interno, tú no lo gestionas)
            val existente = productoDao.getByNombre(input.productoNombre.trim())
            val productoId = if (existente != null) {
                existente.id
            } else {
                val nuevo = ProductoEntity(
                    id = UUID.randomUUID().toString(),
                    nombre = input.productoNombre.trim(),
                    unidad = input.unidad.trim().uppercase(),
                    precioVenta = 0L // no lo usaremos por ahora
                )
                productoDao.insert(nuevo)
                nuevo.id
            }

            // 2) Guardar compra
            val compraId = UUID.randomUUID().toString()
            compraDao.insert(
                CompraCafeEntity(
                    id = compraId,
                    fecha = System.currentTimeMillis(),
                    productoId = productoId,
                    cantidad = input.cantidad,
                    precioUnitCompra = input.precioUnitCompra,
                    proveedor = input.proveedor?.trim()?.ifBlank { null },
                    esCredito = input.esCredito,
                    nota = input.nota?.trim()?.ifBlank { null }
                )
            )

            // 3) Kardex ENTRADA (sube inventario)
            inventarioDao.insertMov(
                KardexMovimientoEntity(
                    id = UUID.randomUUID().toString(),
                    fecha = System.currentTimeMillis(),
                    productoId = productoId,
                    tipo = "ENTRADA",
                    cantidad = input.cantidad,
                    costoUnit = input.precioUnitCompra,
                    docTipo = "COMPRA",
                    docId = compraId,
                    nota = input.nota
                )
            )
        }
    }
}