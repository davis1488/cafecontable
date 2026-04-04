package com.ethandev.cafecontable.data.repository

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaEntity
import com.ethandev.cafecontable.domain.repository.PreparacionEntregaRepository
import com.ethandev.cafecontable.domain.repository.RegistrarPreparacionEntregaInput
import java.util.UUID

class PreparacionEntregaRepositoryImpl(
    private val db: AppDatabase
) : PreparacionEntregaRepository {

    override suspend fun registrarPreparacion(input: RegistrarPreparacionEntregaInput) {
        db.withTransaction {
            val ventaPedidoDao = db.ventaPedidoDao()
            val productoDao = db.productoDao()
            val inventarioDao = db.inventarioDao()
            val preparacionDao = db.preparacionEntregaDao()

            val venta = ventaPedidoDao.obtenerPorId(input.ventaId)
                ?: throw IllegalStateException("No se encontró el pedido")

            val cantidadCafe = input.cantidadCafe
            val cantidadPasilla = input.cantidadPasilla
            val cantidadRegular = input.cantidadRegular

            val totalPreparado = cantidadCafe + cantidadPasilla + cantidadRegular

            require(totalPreparado > 0.0) {
                "Debes ingresar una cantidad mayor a 0"
            }

//            val saldoPendiente = venta.cantidadPactada - venta.cantidadEntregada
//            require(totalPreparado <= saldoPendiente) {
//                "La preparación supera el saldo pendiente de la venta"
//            }

            val productoCafe = productoDao.getByNombre("Cafe")
                ?: throw IllegalStateException("No existe el producto Cafe")

            val productoPasilla = productoDao.getByNombre("Pasilla")
                ?: throw IllegalStateException("No existe el producto Pasilla")

            val productoRegular = productoDao.getByNombre("Regular")
                ?: throw IllegalStateException("No existe el producto Regular")

            val existenciaCafe = inventarioDao.existencia(productoCafe.id)
            val existenciaPasilla = inventarioDao.existencia(productoPasilla.id)
            val existenciaRegular = inventarioDao.existencia(productoRegular.id)

            require(cantidadCafe <= existenciaCafe) {
                "No hay suficiente inventario de Cafe"
            }
            require(cantidadPasilla <= existenciaPasilla) {
                "No hay suficiente inventario de Pasilla"
            }
            require(cantidadRegular <= existenciaRegular) {
                "No hay suficiente inventario de Regular"
            }

            val costoUnitCafe = obtenerCostoUnitarioPromedio(inventarioDao, productoCafe.id)
            val costoUnitPasilla = obtenerCostoUnitarioPromedio(inventarioDao, productoPasilla.id)
            val costoUnitRegular = obtenerCostoUnitarioPromedio(inventarioDao, productoRegular.id)

            val costoCafe = if (cantidadCafe > 0) (cantidadCafe * costoUnitCafe).toLong() else 0L
            val costoPasilla = if (cantidadPasilla > 0) (cantidadPasilla * costoUnitPasilla).toLong() else 0L
            val costoRegular = if (cantidadRegular > 0) (cantidadRegular * costoUnitRegular).toLong() else 0L

            val costoTotal = costoCafe + costoPasilla + costoRegular
            val costoPromedio = if (totalPreparado > 0) {
                (costoTotal / totalPreparado).toLong()
            } else {
                0L
            }

            val preparacionId = UUID.randomUUID().toString()
            val fecha = System.currentTimeMillis()
            val nota = input.nota?.trim()?.ifBlank { null }

            preparacionDao.insertPreparacion(
                PreparacionEntregaEntity(
                    id = preparacionId,
                    ventaId = venta.id,
                    fecha = fecha,
                    cantidadPreparada = totalPreparado,
                    costoTotal = costoTotal,
                    costoPromedio = costoPromedio,
                    precioVentaUnitario = 0L,
                    subtotalVenta = 0L,
                    factor = 90.0,
                    ajusteFactor = 0L,
                    totalFinal = 0L,
                    margen = 0L,
                    estado = "REGISTRADA",
                    nota = nota
                )
            )

            val detalles = mutableListOf<PreparacionEntregaDetalleEntity>()

            if (cantidadCafe > 0) {
                detalles.add(
                    PreparacionEntregaDetalleEntity(
                        id = UUID.randomUUID().toString(),
                        preparacionId = preparacionId,
                        productoId = productoCafe.id,
                        productoNombre = productoCafe.nombre,
                        cantidadUsada = cantidadCafe,
                        costoUnit = costoUnitCafe,
                        subtotal = (cantidadCafe * costoUnitCafe).toLong()
                    )
                )

                inventarioDao.insertMov(
                    KardexMovimientoEntity(
                        id = UUID.randomUUID().toString(),
                        fecha = fecha,
                        productoId = productoCafe.id,
                        tipo = "SALIDA",
                        cantidad = cantidadCafe,
                        costoUnit = costoUnitCafe,
                        docTipo = "PREPARACION",
                        docId = preparacionId,
                        nota = "Preparación pedido ${venta.id}"
                    )
                )
            }

            if (cantidadPasilla > 0) {
                detalles.add(
                    PreparacionEntregaDetalleEntity(
                        id = UUID.randomUUID().toString(),
                        preparacionId = preparacionId,
                        productoId = productoPasilla.id,
                        productoNombre = productoPasilla.nombre,
                        cantidadUsada = cantidadPasilla,
                        costoUnit = costoUnitPasilla,
                        subtotal = (cantidadPasilla * costoUnitPasilla).toLong()
                    )
                )

                inventarioDao.insertMov(
                    KardexMovimientoEntity(
                        id = UUID.randomUUID().toString(),
                        fecha = fecha,
                        productoId = productoPasilla.id,
                        tipo = "SALIDA",
                        cantidad = cantidadPasilla,
                        costoUnit = costoUnitPasilla,
                        docTipo = "PREPARACION",
                        docId = preparacionId,
                        nota = "Preparación pedido ${venta.id}"
                    )
                )
            }

            if (cantidadRegular > 0) {
                detalles.add(
                    PreparacionEntregaDetalleEntity(
                        id = UUID.randomUUID().toString(),
                        preparacionId = preparacionId,
                        productoId = productoRegular.id,
                        productoNombre = productoRegular.nombre,
                        cantidadUsada = cantidadRegular,
                        costoUnit = costoUnitRegular,
                        subtotal = (cantidadRegular * costoUnitRegular).toLong()
                    )
                )

                inventarioDao.insertMov(
                    KardexMovimientoEntity(
                        id = UUID.randomUUID().toString(),
                        fecha = fecha,
                        productoId = productoRegular.id,
                        tipo = "SALIDA",
                        cantidad = cantidadRegular,
                        costoUnit = costoUnitRegular,
                        docTipo = "PREPARACION",
                        docId = preparacionId,
                        nota = "Preparación pedido ${venta.id}"
                    )
                )
            }

            preparacionDao.insertDetalles(detalles)

            val nuevaCantidadEntregada = venta.cantidadAsignada + totalPreparado
            val nuevoEstado = when {
                nuevaCantidadEntregada <= 0.0 -> "PENDIENTE_PREPARACION"
                nuevaCantidadEntregada < venta.cantidadPactada -> "ENTREGA_PARCIAL"
                else -> "ENTREGADA"
            }

            ventaPedidoDao.actualizarEntregaYEstado(
                pedidoId = venta.id,
                cantidadAsignada = nuevaCantidadEntregada,
                estado = nuevoEstado
            )
        }
    }

    private suspend fun obtenerCostoUnitarioPromedio(
        inventarioDao: InventarioDao,
        productoId: String
    ): Long {
        val historial = inventarioDao.historial(productoId)
        val entradas = historial.filter { it.tipo == "ENTRADA" || it.tipo == "AJUSTE" }

        if (entradas.isEmpty()) return 0L

        val totalCantidad = entradas.sumOf { it.cantidad }
        if (totalCantidad <= 0.0) return 0L

        val totalCosto = entradas.sumOf { it.cantidad * it.costoUnit }
        return (totalCosto / totalCantidad).toLong()
    }
}