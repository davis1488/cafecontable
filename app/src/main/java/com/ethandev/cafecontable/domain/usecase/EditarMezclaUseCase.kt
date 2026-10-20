
package com.ethandev.cafecontable.domain.usecase

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
import com.ethandev.cafecontable.domain.constants.EstadoMezcla
import com.ethandev.cafecontable.domain.model.EditarMezclaInput
import java.util.UUID

class EditarMezclaUseCase(
    private val db: AppDatabase
) {
    suspend operator fun invoke(input: EditarMezclaInput) {
        db.withTransaction {
            val mezclaDao = db.mezclaDao()
            val compraDao = db.compraDao()
            val inventarioDao = db.inventarioDao()

            val mezcla = mezclaDao.obtenerMezclaPorId(input.mezclaId)
                ?: error("La mezcla no existe")

            require(mezcla.estado == EstadoMezcla.CREADO.name) {
                "Solo se pueden editar mezclas en estado CREADO"
            }

            require(input.items.isNotEmpty()) {
                "La mezcla debe contener al menos una compra"
            }

            require(
                input.items.map { it.compraId }.distinct().size ==
                        input.items.size
            ) {
                "No puedes repetir una compra en la misma mezcla"
            }

            val anteriores =
                mezclaDao.obtenerDetallesPorMezclaId(input.mezclaId)

            val anteriorPorCompra =
                anteriores.associateBy { it.compraId }

            val comprasValidadas = input.items.associate { item ->
                require(
                    item.cantidadUsada.isFinite() &&
                            item.cantidadUsada > 0.0
                ) {
                    "La cantidad debe ser mayor a cero"
                }

                val compra = compraDao.obtenerCompraParaEdicion(
                    compraId = item.compraId,
                    mezclaId = input.mezclaId
                ) ?: error("Compra no disponible: ${item.compraId}")

                require(compra.productoId == item.productoId) {
                    "El producto de la compra no coincide"
                }

                require(item.cantidadUsada <= compra.cantidadDisponible + 0.000001) {
                    "Cantidad insuficiente en compra ${item.compraId}"
                }

                item.compraId to compra
            }

            val nuevaCantidadPorProducto = input.items
                .groupBy { it.productoId }
                .mapValues { (_, lista) ->
                    lista.sumOf { it.cantidadUsada }
                }

            val anteriorCantidadPorProducto = anteriores
                .groupBy { it.productoId }
                .mapValues { (_, lista) ->
                    lista.sumOf { it.cantidadUsada }
                }

            val productos = (
                    nuevaCantidadPorProducto.keys +
                            anteriorCantidadPorProducto.keys
                    ).distinct()

            // Validar existencia después de aplicar las diferencias.
            productos.forEach { productoId ->
                val anterior =
                    anteriorCantidadPorProducto[productoId] ?: 0.0
                val nueva =
                    nuevaCantidadPorProducto[productoId] ?: 0.0

                val existencia = inventarioDao.existencia(productoId)

                require(existencia + anterior - nueva >= -0.000001) {
                    "Inventario insuficiente para el producto $productoId"
                }
            }

            // Preparar detalles nuevos conservando IDs existentes.
            val detallesActualizados = input.items.map { item ->
                val compra = comprasValidadas.getValue(item.compraId)
                val anterior = anteriorPorCompra[item.compraId]

                MezclaDetalleEntity(
                    id = anterior?.id ?: UUID.randomUUID().toString(),
                    mezclaId = input.mezclaId,
                    compraId = item.compraId,
                    productoId = compra.productoId,
                    productoNombre = compra.productoNombre,
                    cantidadUsada = item.cantidadUsada,
                    costoUnitCompra = compra.precioUnitCompra
                )
            }

            val nuevaCantidad = detallesActualizados.sumOf {
                it.cantidadUsada
            }

            val nuevoCosto = detallesActualizados.sumOf {
                it.cantidadUsada * it.costoUnitCompra.toDouble()
            }.toLong()

            val nuevoPromedio =
                (nuevoCosto / nuevaCantidad).toLong()

            // Corregir los movimientos de cada compra.
            val nuevasPorCompra =
                detallesActualizados.associateBy { it.compraId }

            val todasLasCompras = (
                    anteriorPorCompra.keys + nuevasPorCompra.keys
                    ).distinct()

            todasLasCompras.forEach { compraId ->
                val anterior = anteriorPorCompra[compraId]
                val nuevo = nuevasPorCompra[compraId]

                val diferencia =
                    (nuevo?.cantidadUsada ?: 0.0) -
                            (anterior?.cantidadUsada ?: 0.0)

                if (kotlin.math.abs(diferencia) > 0.000001) {
                    val referencia = nuevo ?: anterior!!

                    inventarioDao.insertMov(
                        KardexMovimientoEntity(
                            id = UUID.randomUUID().toString(),
                            fecha = System.currentTimeMillis(),
                            productoId = referencia.productoId,
                            tipo = if (diferencia > 0) {
                                "SALIDA"
                            } else {
                                "ENTRADA"
                            },
                            cantidad = kotlin.math.abs(diferencia),
                            costoUnit = referencia.costoUnitCompra,
                            docTipo = "MEZCLA",
                            docId = input.mezclaId,
                            nota = "Corrección de mezcla - compra $compraId"
                        )
                    )
                }
            }

            // Reemplazar detalles dentro de la misma transacción.
            mezclaDao.eliminarDetallesPorMezclaId(input.mezclaId)
            mezclaDao.insertarDetallesMezcla(detallesActualizados)

            mezclaDao.actualizarTotalesMezcla(
                mezclaId = input.mezclaId,
                cantidadTotal = nuevaCantidad,
                costoTotal = nuevoCosto,
                costoPromedioKg = nuevoPromedio
            )

            check(
                mezclaDao.actualizarNotaMezcla(
                    mezclaId = input.mezclaId,
                    nota = input.nota?.trim()?.ifBlank { null }
                ) == 1
            ) {
                "No se pudo actualizar la nota de la mezcla"
            }
        }
    }
}
