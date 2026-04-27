package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.GastoOperacionDao
import com.ethandev.cafecontable.data.local.dao.MezclaDao
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
    private val mezclaDao: MezclaDao

) : UtilidadOperacionRepository {

//    override suspend fun listarPendientes(): List<UtilidadPendienteModel> {
//        return utilidadDao.listarPendientes()
//    }
override suspend fun listarPendientes(): List<UtilidadPendienteModel> {
    return utilidadDao.listarPendientes().map { item ->

        val gastosCompra = mezclaDao
            .totalGastosCompraProrrateadosPorMezcla(item.mezclaId)
            .roundToLong()

        val gastosMezcla = item.operacionMezclaId?.let {
            gastoDao.totalPorOperacion(it)
        } ?: 0L

        val gastosEntrega = item.operacionEntregaId?.let {
            gastoDao.totalPorOperacion(it)
        } ?: 0L

        item.copy(
            gastosCompra = gastosCompra,
            gastosMezcla = gastosMezcla,
            gastosEntrega = gastosEntrega,
            totalGastos = gastosCompra + gastosMezcla + gastosEntrega
        )
    }
}

    override suspend fun listarHistorial(): List<UtilidadOperacionModel> {
        return utilidadDao.listarHistorial()
    }

    override suspend fun calcularYGuardarUtilidad(
        pendiente: UtilidadPendienteModel,
        nota: String?
    ) {

        val costoCafe = (pendiente.cantidadKg * pendiente.costoPromedioKg).roundToLong()
//
//        val gastosCompra = gastoDao.totalPorOperacion(
//            operacionId = pendiente.mezclaId,
////            categoria = TipoGastoOperacion.COMPRA
//        )
//
//        val gastosMezcla = gastoDao.totalPorOperacion(
//            operacionId = pendiente.mezclaId,
////            categoria = TipoGastoOperacion.MEZCLA
//        )
//
//        val gastosEntrega = gastoDao.totalPorOperacion(
//            operacionId = pendiente.liquidacionId,
////            categoria = TipoGastoOperacion.ENTREGA
//        )
//
//        val otrosGastos = gastoDao.totalPorOperacion(
//            operacionId = pendiente.liquidacionId,
////            categoria = TipoGastoOperacion.OTRO
//        )
//
//        val totalGastos = gastosCompra + gastosMezcla + gastosEntrega + otrosGastos

        val gastosCompra = pendiente.operacionCompraId?.let {
            gastoDao.totalPorOperacion(it)
        } ?: 0L

        val gastosMezcla = pendiente.operacionMezclaId?.let {
            gastoDao.totalPorOperacion(it)
        } ?: 0L

        val gastosEntrega = pendiente.operacionEntregaId?.let {
            gastoDao.totalPorOperacion(it)
        } ?: 0L

        val otrosGastos = 0L

        val totalGastos = gastosCompra + gastosMezcla + gastosEntrega + otrosGastos

        println("mezclaId: ${pendiente.mezclaId}")
        println("liquidacionId: ${pendiente.liquidacionId}")
        println("gastosMezcla: $gastosMezcla")
        println("gastosEntrega: $gastosEntrega")

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