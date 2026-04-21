package com.ethandev.cafecontable.data.repository

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.dao.MezclaDao
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.MezclaEntity
import com.ethandev.cafecontable.domain.constants.EstadoMezcla
import com.ethandev.cafecontable.domain.model.MezclaHistorialItem
import com.ethandev.cafecontable.domain.model.RegistrarMezclaInput
import com.ethandev.cafecontable.domain.repository.MezclaRepository

import java.util.UUID

class MezclaRepositoryImpl(
    private val  db: AppDatabase,

    private val  mezclaDao : MezclaDao,

    private val  inventarioDao :InventarioDao

) : MezclaRepository {

    override suspend fun obtenerHistorialMezclas(): List<MezclaHistorialItem> {
        return mezclaDao.obtenerHistorialMezclas().map { item ->
            MezclaHistorialItem(
                id = item.id,
                fechaTexto = formatearFecha(item.fecha),
                cantidadTotal = item.cantidadTotal,
                costoTotal = item.costoTotal,
                estado = item.estado,
                nota = item.nota
            )
        }
    }

    private fun formatearFecha(fecha: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(fecha))
    }

    override suspend fun registrarMezcla(input: RegistrarMezclaInput) {
        db.withTransaction {
//            val inventarioDao = db.inventarioDao()
//            val mezclaDao = db.mezclaDao()

            require(input.items.isNotEmpty()) {
                "Debes agregar al menos un item a la mezcla"
            }

            input.items.forEach { item ->
                require(item.compraId.isNotBlank()) {
                    "La compra es obligatoria"
                }

                require(item.productoId.isNotBlank()) {
                    "El producto es obligatorio"
                }

                require(item.productoNombre.isNotBlank()) {
                    "El nombre del producto es obligatorio"
                }

                require(item.cantidadUsada > 0.0) {
                    "La cantidad usada debe ser mayor a cero"
                }

                require(item.costoUnitCompra >= 0L) {
                    "El costo unitario no puede ser negativo"
                }

                val existencia = inventarioDao.existencia(item.productoId)
                require(item.cantidadUsada <= existencia) {
                    "Inventario insuficiente para ${item.productoNombre}. Disponible: $existencia"
                }
            }

            val cantidadTotal = input.items.sumOf { it.cantidadUsada }

            require(cantidadTotal > 0.0) {
                "La cantidad total de la mezcla debe ser mayor a cero"
            }

            val costoTotal = input.items.sumOf { item ->
                item.cantidadUsada * item.costoUnitCompra.toDouble()
            }.toLong()

            val costoPromedioKg = (costoTotal / cantidadTotal).toLong()
            val mezclaId = UUID.randomUUID().toString()
            val nota = input.nota?.trim()?.ifBlank { null }

            mezclaDao.insertMezcla(
                MezclaEntity(
                    id = mezclaId,
                    fecha = input.fecha,
                    cantidadTotal = cantidadTotal,
                    cantidadDisponible = cantidadTotal,
                    costoTotal = costoTotal,
                    costoPromedioKg = costoPromedioKg,
                    estado = EstadoMezcla.CREADO.toString(),
                    nota = nota,
                    operacionMezclaId = input.operacionMezclaId,
                    costoCafeBase = costoTotal,
                    gastosCompraAcumulados = 0L,
                    //gastosMezcla = 0L



                )
            )

            val detalles = input.items.map { item ->
                MezclaDetalleEntity(
                    id = UUID.randomUUID().toString(),
                    mezclaId = mezclaId,
                    compraId = item.compraId,
                    productoId = item.productoId,
                    productoNombre = item.productoNombre,
                    cantidadUsada = item.cantidadUsada,
                    costoUnitCompra = item.costoUnitCompra,
                    subtotal = (item.cantidadUsada * item.costoUnitCompra.toDouble()).toLong()
                )
            }

            mezclaDao.insertDetalles(detalles)

            input.items.forEach { item ->
                inventarioDao.insertMov(
                    KardexMovimientoEntity(
                        id = UUID.randomUUID().toString(),
                        fecha = input.fecha,
                        productoId = item.productoId,
                        tipo = "SALIDA",
                        cantidad = item.cantidadUsada,
                        costoUnit = item.costoUnitCompra,
                        docTipo = "MEZCLA",
                        docId = mezclaId,
                        nota = "Salida por creación de mezcla"
                    )
                )
            }
        }
    }

    override suspend fun actualizarEstadoMezcla(
        mezclaId: String,
        estado: String
    ): Int {
        return mezclaDao.actualizarEstadoMezcla(
            mezclaId = mezclaId,
            estado = estado
        )
    }

    override suspend fun marcarPendienteEntrega(
        mezclaId: String,
        numeroSacosEnviados: Int,
        kilajeEnviado: Double
    ): Int {
        return mezclaDao.marcarPendienteEntrega(
            mezclaId = mezclaId,
            estado = EstadoMezcla.PENDIENTE_ENTREGA.toString(),
            numeroSacosEnviados = numeroSacosEnviados,
            kilajeEnviado = kilajeEnviado
        )
    }

    override suspend fun marcarEntregado(
        mezclaId: String,
        numeroSacosEntregados: Int,
        kilajeEntregado: Double,
        lugarEntrega: String
    ): Int {
        return mezclaDao.marcarEntregado(
            mezclaId = mezclaId,
            estado = EstadoMezcla.ENTREGADO.toString(),
            numeroSacosEntregados = numeroSacosEntregados,
            kilajeEntregado = kilajeEntregado,
            lugarEntrega = lugarEntrega
        )
    }

    override suspend fun marcarAnalizado(
        mezclaId: String,
        factorRendimiento: Double
    ): Int {
        return mezclaDao.marcarAnalizado(
            mezclaId = mezclaId,
            estado = EstadoMezcla.ANALIZADO.toString(),
            factorRendimiento = factorRendimiento
        )
    }



}