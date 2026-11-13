package com.ethandev.cafecontable.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.AbonoCuentaPorPagarEntity
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.CuentaPorPagarEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.OperacionPendienteSyncEntity
import com.ethandev.cafecontable.data.local.entity.ProductoEntity
import com.ethandev.cafecontable.data.local.session.SessionManager
import com.ethandev.cafecontable.domain.model.CompraCafeImput
import com.ethandev.cafecontable.domain.repository.CompraRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.util.UUID

class CompraRepositoryImpl(
    private val db: AppDatabase,
    private val sessionManager: SessionManager

) : CompraRepository {

    override suspend fun registrarCompra(input: CompraCafeImput) {

        val sesion = sessionManager.usuarioSesion.first()
            ?: throw IllegalStateException(
                "Debes iniciar sesión para registrar compras"
            )

        val operacionSyncId = UUID.randomUUID().toString()

        db.withTransaction {
            val productoDao = db.productoDao()
            val inventarioDao = db.inventarioDao()
            val compraDao = db.compraDao()
            val cuentaPorPagarDao = db.cuentaPorPagarDao()

            val nombreProducto = input.productoNombre.trim()
            val unidadProducto = input.unidad.trim().uppercase()
            val proveedor = input.proveedor?.trim()?.ifBlank { null }

            val abono = input.abono


            val nota = input.nota?.trim()?.ifBlank { null }

            val existente = productoDao.getByNombre(nombreProducto)

            val productoId = if (existente != null) {
                existente.id
            } else {
                val nuevo = ProductoEntity(
                    id = UUID.randomUUID().toString(),
                    nombre = nombreProducto,
                    unidad = unidadProducto,
                    precioVenta = 0L
                )
                productoDao.insert(nuevo)
                nuevo.id
            }


            val operacionCompraId = input.operacionCompraId?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException(
                    "Debe seleccionar una operación activa"
                )

            val operacion = db.operacionDao()
                .obtenerPorId(operacionCompraId)
                ?: throw IllegalStateException(
                    "La operación seleccionada no existe"
                )

            if (operacion.tipo != "COMPRA") {
                throw IllegalStateException(
                    "La operación debe ser de tipo COMPRA"
                )
            }

            val compraId = compraDao.insert(
                CompraCafeEntity(
                    id = 0,
                    fecha = System.currentTimeMillis(),
                    productoId = productoId,
                    cantidad = input.cantidad,
                    precioUnitCompra = input.precioUnitCompra,
                    proveedor = proveedor,
                    esCredito = input.esCredito,
                    nota = nota,
                    operacionCompraId = operacionCompraId
                )
            ).toString()

            Log.d("DEBUG_COMPRA", "compraId real = $compraId")
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
                    nota = nota
                )
            )

            if (input.esCredito) {

                if (proveedor.isNullOrBlank()) {
                    throw IllegalStateException("Debes ingresar el proveedor para compras a crédito")
                }

                val totalCompra = (input.cantidad * input.precioUnitCompra).toLong()
                val idCompra = UUID.randomUUID().toString()
                cuentaPorPagarDao.insert(
                    CuentaPorPagarEntity(
                        id = idCompra,
                        fecha = System.currentTimeMillis(),
                        compraId = compraId,
                        proveedor = proveedor,
                        valorInicial = totalCompra,
                        saldoPendiente = totalCompra-abono,
                        estado = "PENDIENTE",
                        nota = nota
                    )
                )

                if (input.abono > 0) {
                    cuentaPorPagarDao.insertAbono(
                        AbonoCuentaPorPagarEntity(
                            id = UUID.randomUUID().toString(),
                            cuentaId = idCompra,
                            fecha = System.currentTimeMillis(),
                            valor = abono,
                            nota = input.nota?.trim()?.ifBlank { null }
                        )
                    )
                }

            }



            val payload = Gson().toJson(
                mapOf(
                    "operacionSyncId" to operacionSyncId,
                    "compraLocalId" to compraId,

                    // Datos del producto
                    "productoNombre" to nombreProducto,
                    "unidad" to unidadProducto,

                    // Datos de la compra
                    "cantidad" to input.cantidad,
                    "precioUnitCompra" to input.precioUnitCompra,
                    "proveedor" to proveedor,
                    "esCredito" to input.esCredito,
                    "nota" to nota,
                    "abono" to abono,

                    // Datos reales de la operación
                    "operacionCompraId" to operacion.id,
                    "operacionNombre" to operacion.nombre,
                    "operacionTipo" to operacion.tipo,
                    "operacionDescripcion" to operacion.descripcion,
                    "operacionFecha" to operacion.fecha,
                    "operacionEstado" to operacion.estado.name
                )
            )


            db.operacionPendienteSyncDao().insertar(
                OperacionPendienteSyncEntity(
                    id = operacionSyncId,
                    empresaId = sesion.empresaId,
                    usuarioId = sesion.usuarioId,
                    tipoOperacion = "COMPRA",
                    referenciaLocal = compraId,
                    payloadJson = payload,
                    estado = "PENDIENTE"
                )
            )
        }
    }
}