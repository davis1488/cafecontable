package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.GastoOperacionDao
import com.ethandev.cafecontable.data.local.dao.MezclaDao
import com.ethandev.cafecontable.data.local.dao.UtilidadOperacionDao
import com.ethandev.cafecontable.data.local.entity.UtilidadOperacionEntity
import com.ethandev.cafecontable.domain.model.UtilidadOperacionModel
import com.ethandev.cafecontable.domain.model.UtilidadPendienteModel
import com.ethandev.cafecontable.domain.repository.ResumenUtilidadModel
import com.ethandev.cafecontable.domain.repository.UtilidadOperacionRepository
import java.util.UUID
import kotlin.math.roundToLong

/**
 * Imputa los gastos de compra y mezcla proporcionalmente a los kg liquidados.
 *
 * IMPORTANTE: operacionEntregaId llega como NULL desde UtilidadOperacionDao.listarPendientes().
 * No se imputa un gasto de entrega a una liquidación sin una relación verificable.
 */
class UtilidadOperacionRepositoryImpl(
    private val utilidadDao: UtilidadOperacionDao,
    private val gastoDao: GastoOperacionDao,
    private val mezclaDao: MezclaDao
) : UtilidadOperacionRepository {

    override suspend fun listarPendientes(): List<UtilidadPendienteModel> {
        return utilidadDao.listarPendientes().map { item ->
            val mezcla = mezclaDao.getById(item.mezclaId)
                ?: error("No se encontró la mezcla ${item.mezclaId}")
            require(mezcla.cantidadTotal > 0.0) {
                "La mezcla ${item.mezclaId} no tiene una cantidad total válida"
            }

            val proporción = (item.cantidadKg / mezcla.cantidadTotal)
            require(proporción > 0.0 && proporción <= 1.0 + 1e-8) {
                "La cantidad liquidada supera la cantidad de la mezcla"
            }
            val fracción = proporción.coerceAtMost(1.0)

            // La consulta SQL distribuye los gastos de las operaciones de COMPRA
            // entre las compras utilizadas en esta mezcla.
            val gastoCompraMezcla = mezclaDao
                .totalGastosCompraProrrateadosPorMezcla(item.mezclaId)

            val gastoMezclaCompleta = mezclaDao
                .totalGastosOperacionMezclaProrrateados(item.mezclaId)

            // No se puede inferir la operación de entrega a partir del ID de
            // liquidación o mezcla: solo se imputa cuando hay un ID explícito.
            val gastoEntrega = item.operacionEntregaId?.let { operacionId ->
                gastoDao.totalPorOperacion(operacionId)
            } ?: 0L

            val gastosCompra = (gastoCompraMezcla * fracción).roundToLong()
            val gastosMezcla = (gastoMezclaCompleta * fracción).roundToLong()

            item.copy(
                gastosCompra = gastosCompra,
                gastosMezcla = gastosMezcla,
                gastosEntrega = gastoEntrega,
                totalGastos = gastosCompra + gastosMezcla + gastoEntrega
            )
        }
    }

    override suspend fun listarHistorial(): List<UtilidadOperacionModel> =
        utilidadDao.listarHistorial()

    override suspend fun calcularYGuardarUtilidad(
        pendiente: UtilidadPendienteModel,
        nota: String?
    ) {
        // Reconstruir desde la BD evita confiar en importes precalculados
        // desactualizados o en los IDs NULL que genera el DAO inicial.
        val actualizado = listarPendientes()
            .firstOrNull { it.liquidacionId == pendiente.liquidacionId }
            ?: error("La liquidación ya fue calculada o no está disponible")

        val costoCafe = (actualizado.cantidadKg * actualizado.costoPromedioKg).roundToLong()
        val gastosCompra = actualizado.gastosCompra
        val gastosMezcla = actualizado.gastosMezcla
        val gastosEntrega = actualizado.gastosEntrega
        val otrosGastos = 0L
        val totalGastos = gastosCompra + gastosMezcla + gastosEntrega + otrosGastos
        val utilidadBruta = actualizado.valorVentaNeto - costoCafe
        val utilidadNeta = utilidadBruta - totalGastos
        val margen = if (actualizado.valorVentaNeto > 0L) {
            utilidadNeta.toDouble() / actualizado.valorVentaNeto.toDouble() * 100.0
        } else 0.0

        utilidadDao.insertar(
            UtilidadOperacionEntity(
                id = UUID.randomUUID().toString(),
                fecha = System.currentTimeMillis(),
                liquidacionId = actualizado.liquidacionId,
                mezclaId = actualizado.mezclaId,
                pedidoId = actualizado.pedidoId,
                cantidadKg = actualizado.cantidadKg,
                valorVentaBruto = actualizado.valorVentaBruto,
                ajusteFactor = actualizado.ajusteFactor,
                descuentos = actualizado.descuentos,
                valorVentaNeto = actualizado.valorVentaNeto,
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
        )
    }

    override suspend fun obtenerResumen(): ResumenUtilidadModel {
        val ventas = utilidadDao.totalVentasNetas()
        val costoCafe = utilidadDao.totalCostoCafe()
        val gastos = utilidadDao.totalGastos()
        val utilidad = utilidadDao.totalUtilidadNeta()
        val margen = if (ventas > 0L) {
            utilidad.toDouble() / ventas.toDouble() * 100.0
        } else 0.0
        return ResumenUtilidadModel(
            ventasNetas = ventas,
            costoCafe = costoCafe,
            gastos = gastos,
            utilidadNeta = utilidad,
            margenPorcentaje = margen
        )
    }
}
