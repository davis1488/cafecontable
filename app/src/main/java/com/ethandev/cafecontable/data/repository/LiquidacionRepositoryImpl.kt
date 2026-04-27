package com.ethandev.cafecontable.data.repository

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.dao.LiquidacionDao
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.LiquidacionEntregaEntity
import com.ethandev.cafecontable.domain.constants.EstadoAnuncio
import com.ethandev.cafecontable.domain.constants.EstadoMezcla
import com.ethandev.cafecontable.domain.model.LiquidacionHistorial
import com.ethandev.cafecontable.domain.model.LiquidacionPendiente
import com.ethandev.cafecontable.domain.repository.LiquidacionRepository
import com.ethandev.cafecontable.domain.repository.dto.RegistrarLiquidacionInput
import java.util.UUID
import kotlin.math.roundToLong

class LiquidacionRepositoryImpl(
    private val  db: AppDatabase,
    private val liquidacionDao: LiquidacionDao
) : LiquidacionRepository {

    override suspend fun obtenerPendientes(): List<LiquidacionPendiente> {
        println("Estado pedido enviado: ${EstadoAnuncio.ENTREGA_TOTAL}")
        println("Estado pedido name: ${EstadoAnuncio.ENTREGA_TOTAL.name}")
        println("Estado mezcla name: ${EstadoMezcla.ANALIZADO.name}")
        return liquidacionDao.obtenerPendientesLiquidacion(
            estadoPedido = EstadoAnuncio.ENTREGA_TOTAL.toString(),
            estadoMezcla = EstadoMezcla.ANALIZADO.toString()
        ).map {
            LiquidacionPendiente(
                asignacionId = it.asignacionId,
                pedidoId = it.pedidoId,
                mezclaId = it.mezclaId,
                cliente = it.cliente,
                cantidadKg = it.cantidadKg,
                precioBaseKg = it.precioBaseKg,
                factorReal = it.factorReal
            )
        }
    }

    override suspend fun obtenerHistorial(): List<LiquidacionHistorial> {
        return liquidacionDao.obtenerHistorialLiquidaciones().map {
            LiquidacionHistorial(
                id = it.id,
                fecha = it.fecha,
                asignacionId = it.asignacionId,
                pedidoId = it.pedidoId,
                mezclaId = it.mezclaId,
                cliente = it.cliente,
                cantidadKg = it.cantidadKg,
                precioBaseKg = it.precioBaseKg,
                factorReal = it.factorReal,
                valorBase = it.valorBase,
                ajusteFactor = it.ajusteFactor,
                descuentoCooperativa = it.descuentoCooperativa,
                otrosDescuentos = it.otrosDescuentos,
                valorNeto = it.valorNeto,
                nota = it.nota
            )
        }
    }

    override suspend fun registrarLiquidacion(input: RegistrarLiquidacionInput) {
        db.withTransaction {
            val yaExiste = liquidacionDao.obtenerPorAsignacion(input.asignacionId)
            if (yaExiste != null) {
                throw IllegalStateException("Esa asignación ya fue liquidada")
            }

            if (input.cantidadKg <= 0.0) {
                throw IllegalStateException("La cantidad debe ser mayor a 0")
            }

            if (input.precioBaseKg <= 0L) {
                throw IllegalStateException("El precio base debe ser mayor a 0")
            }

            val valorBase = (input.cantidadKg * input.precioBaseKg.toDouble()).roundToLong()

            // Regla:
            // 90 = base
            // >90 = descuento
            // <90 = bonificación
            val diferencia = 90.0 - input.factorReal
            val ajusteFactor = ((valorBase * diferencia) / 100.0).roundToLong()

            val valorNeto = valorBase +
                    ajusteFactor -
                    input.descuentoCooperativa -
                    input.otrosDescuentos

            if (valorNeto < 0L) {
                throw IllegalStateException("El valor neto no puede quedar negativo")
            }

            liquidacionDao.insert(
                LiquidacionEntregaEntity(
                    id = UUID.randomUUID().toString(),
                    asignacionId = input.asignacionId,
                    pedidoId = input.pedidoId,
                    mezclaId = input.mezclaId,
                    fecha = System.currentTimeMillis(),
                    cliente = input.cliente,
                    cantidadKg = input.cantidadKg,
                    precioBaseKg = input.precioBaseKg,
                    factorReal = input.factorReal,
                    valorBase = valorBase,
                    ajusteFactor = ajusteFactor,
                    descuentoCooperativa = input.descuentoCooperativa,
                    otrosDescuentos = input.otrosDescuentos,
                    valorNeto = valorNeto,
                    nota = input.nota?.trim()?.ifBlank { null }
                )
            )

            db.ventaPedidoDao().actualizarEstado(
                pedidoId = input.pedidoId,
                estado = EstadoAnuncio.LIQUIDADO.toString()
            )
        }
    }
}