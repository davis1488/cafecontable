package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.GastoOperacionDao
import com.ethandev.cafecontable.data.local.dao.MezclaDao
import com.ethandev.cafecontable.data.local.dao.MezclaDespachoDao
import com.ethandev.cafecontable.data.local.dao.UtilidadOperacionDao
import com.ethandev.cafecontable.data.local.entity.UtilidadOperacionEntity
import com.ethandev.cafecontable.domain.constants.TipoGastoOperacion
import com.ethandev.cafecontable.domain.model.UtilidadOperacionModel
import com.ethandev.cafecontable.domain.model.UtilidadPendienteModel
import com.ethandev.cafecontable.domain.repository.ResumenUtilidadModel
import com.ethandev.cafecontable.domain.repository.UtilidadOperacionRepository
import java.util.UUID
import kotlin.math.roundToLong

class UtilidadOperacionRepositoryImpl(
    private val utilidadDao: UtilidadOperacionDao,
    private val gastoDao: GastoOperacionDao,
    private val mezclaDao: MezclaDao,
    private val despachoDao: MezclaDespachoDao

) : UtilidadOperacionRepository {

    private suspend fun gastosParaLiquidacion(mezclaId: String, kgLiquidado: Double): Triple<Long, Long, Long> {
        val mezcla = mezclaDao.getById(mezclaId) ?: error("Mezcla no encontrada")
        val totalKg = mezcla.cantidadTotal
        require(totalKg > 0.0 && kgLiquidado > 0.0 && kgLiquidado <= totalKg + 0.000001) {
            "Cantidad liquidada inválida para el prorrateo"
        }
        val fraccion = kgLiquidado / totalKg
        val compra = (mezclaDao.totalGastosCompraProrrateadosPorMezcla(mezclaId) * fraccion).roundToLong()
        val mezclaG = (mezclaDao.totalGastosOperacionMezclaProrrateados(mezclaId) * fraccion).roundToLong()
        val despacho = (despachoDao.gastosDespachoPorMezcla(mezclaId) * fraccion).roundToLong()
        return Triple(compra, mezclaG, despacho)
    }

    override suspend fun listarPendientes(): List<UtilidadPendienteModel> =
        utilidadDao.listarPendientes().map { item ->
            val (gc, gm, gd) = gastosParaLiquidacion(item.mezclaId, item.cantidadKg)
            item.copy(gastosCompra = gc, gastosMezcla = gm, gastosEntrega = gd,
                totalGastos = gc + gm + gd)
        }

    override suspend fun listarHistorial(): List<UtilidadOperacionModel> {
        return utilidadDao.listarHistorial()
    }

    override suspend fun calcularYGuardarUtilidad(
        pendiente: UtilidadPendienteModel,
        nota: String?
    ) {

        val costoCafe = (pendiente.cantidadKg * pendiente.costoPromedioKg).roundToLong()
        val (gastosCompra, gastosMezcla, gastosEntrega) =
            gastosParaLiquidacion(pendiente.mezclaId, pendiente.cantidadKg)
        val otrosGastos = 0L
        val totalGastos = gastosCompra + gastosMezcla + gastosEntrega

        val utilidadBruta = pendiente.valorVentaNeto - costoCafe
        val utilidadNeta = utilidadBruta - totalGastos

        val margen = if (pendiente.valorVentaNeto > 0) {
            (utilidadNeta.toDouble() / pendiente.valorVentaNeto.toDouble()) * 100.0
        } else {
            0.0
        }

        val entity = UtilidadOperacionEntity(
            id = UUID.randomUUID().toString(),
            fecha = System.currentTimeMillis(),
            liquidacionId = pendiente.liquidacionId,
            mezclaId = pendiente.mezclaId,
            pedidoId = pendiente.pedidoId,
            cantidadKg = pendiente.cantidadKg,
            valorVentaBruto = pendiente.valorVentaBruto,
            ajusteFactor = pendiente.ajusteFactor,
            descuentos = pendiente.descuentos,
            valorVentaNeto = pendiente.valorVentaNeto,
            costoCafe = costoCafe,
            gastosCompra = gastosCompra,
            gastosMezcla = gastosMezcla,
            gastosEntrega = gastosEntrega,
            otrosGastos = otrosGastos,
            totalGastos = totalGastos,
            utilidadBruta = utilidadBruta,
            utilidadNeta = utilidadNeta,
            margenPorcentaje = margen,
            nota = nota
        )

        utilidadDao.insertar(entity)
    }

    override suspend fun recalcularUtilidad(liquidacionId: String) {
        val previo = utilidadDao.porLiquidacion(liquidacionId)
            ?: error("No existe una utilidad guardada para esa liquidación")
        val (gc, gm, gd) = gastosParaLiquidacion(previo.mezclaId, previo.cantidadKg)
        val gastos = gc + gm + gd + previo.otrosGastos
        val bruta = previo.valorVentaNeto - previo.costoCafe
        val neta = bruta - gastos
        val margen = if (previo.valorVentaNeto > 0L) neta * 100.0 / previo.valorVentaNeto else 0.0
        utilidadDao.actualizar(previo.copy(
            gastosCompra = gc, gastosMezcla = gm, gastosEntrega = gd,
            totalGastos = gastos, utilidadBruta = bruta,
            utilidadNeta = neta, margenPorcentaje = margen
        ))
    }

    override suspend fun obtenerResumen(): ResumenUtilidadModel {
        val ventas = utilidadDao.totalVentasNetas()
        val costoCafe = utilidadDao.totalCostoCafe()
        val gastos = utilidadDao.totalGastos()
        val utilidad = utilidadDao.totalUtilidadNeta()

        val margen = if (ventas > 0) {
            (utilidad.toDouble() / ventas.toDouble()) * 100.0
        } else {
            0.0
        }

        return ResumenUtilidadModel(
            ventasNetas = ventas,
            costoCafe = costoCafe,
            gastos = gastos,
            utilidadNeta = utilidad,
            margenPorcentaje = margen
        )
    }
}