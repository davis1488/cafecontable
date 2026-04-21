package com.ethandev.cafecontable.data.repository


import com.ethandev.cafecontable.data.local.dao.AbonoPrestamoDao
import com.ethandev.cafecontable.data.local.dao.PrestamoDao
import com.ethandev.cafecontable.data.local.entity.AbonoPrestamoEntity
import com.ethandev.cafecontable.data.local.entity.PrestamoEntity
import com.ethandev.cafecontable.domain.model.AbonoPrestamoModel
import com.ethandev.cafecontable.domain.model.PrestamoModel
import com.ethandev.cafecontable.domain.repository.PrestamoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrestamoRepositoryImpl(
    private val prestamoDao: PrestamoDao,
    private val abonoPrestamoDao: AbonoPrestamoDao
) : PrestamoRepository {

    override suspend fun registrarPrestamo(prestamo: PrestamoModel) {
        prestamoDao.insertarPrestamo(
            PrestamoEntity(
                id = prestamo.id,
                nombrePersona = prestamo.nombrePersona,
                cedulaPersona = prestamo.cedulaPersona,
                fechaPrestamo = prestamo.fechaPrestamo,
                valorPrestado = prestamo.valorPrestado,
                saldoPendiente = prestamo.saldoPendiente,
                interes = prestamo.interes,
                observacion = prestamo.observacion,
                fechaVencimiento = prestamo.fechaVencimiento,
                estado = prestamo.estado
            )
        )
    }

    override suspend fun registrarAbono(abono: AbonoPrestamoModel) {
        abonoPrestamoDao.insertarAbono(
            AbonoPrestamoEntity(
                id = abono.id,
                prestamoId = abono.prestamoId,
                fechaAbono = abono.fechaAbono,
                valorAbono = abono.valorAbono,
                observacion = abono.observacion
            )
        )
    }

    override fun listarPrestamos(): Flow<List<PrestamoModel>> {
        return prestamoDao.listarPrestamos().map { lista ->
            lista.map { entity ->
                PrestamoModel(
                    id = entity.id,
                    nombrePersona = entity.nombrePersona,
                    cedulaPersona = entity.cedulaPersona,
                    fechaPrestamo = entity.fechaPrestamo,
                    valorPrestado = entity.valorPrestado,
                    saldoPendiente = entity.saldoPendiente,
                    interes = entity.interes,
                    observacion = entity.observacion,
                    fechaVencimiento = entity.fechaVencimiento,
                    estado = entity.estado
                )
            }
        }
    }

    override fun buscarPrestamos(query: String): Flow<List<PrestamoModel>> {
        return prestamoDao.buscarPrestamos(query).map { lista ->
            lista.map { entity ->
               PrestamoModel(
                    id = entity.id,
                    nombrePersona = entity.nombrePersona,
                    cedulaPersona = entity.cedulaPersona,
                    fechaPrestamo = entity.fechaPrestamo,
                    valorPrestado = entity.valorPrestado,
                    saldoPendiente = entity.saldoPendiente,
                    interes = entity.interes,
                    observacion = entity.observacion,
                    fechaVencimiento = entity.fechaVencimiento,
                    estado = entity.estado
                )
            }
        }
    }

    override fun listarAbonos(prestamoId: Int): Flow<List<AbonoPrestamoModel>> {
        return abonoPrestamoDao.listarAbonosPorPrestamo(prestamoId).map { lista ->
            lista.map { entity ->
                AbonoPrestamoModel(
                    id = entity.id,
                    prestamoId = entity.prestamoId,
                    fechaAbono = entity.fechaAbono,
                    valorAbono = entity.valorAbono,
                    observacion = entity.observacion
                )
            }
        }
    }

    override suspend fun obtenerPrestamoPorId(id: Int): PrestamoModel? {
        return prestamoDao.obtenerPrestamoPorId(id)?.let { entity ->
            PrestamoModel(
                id = entity.id,
                nombrePersona = entity.nombrePersona,
                cedulaPersona = entity.cedulaPersona,
                fechaPrestamo = entity.fechaPrestamo,
                valorPrestado = entity.valorPrestado,
                saldoPendiente = entity.saldoPendiente,
                interes = entity.interes,
                observacion = entity.observacion,
                fechaVencimiento = entity.fechaVencimiento,
                estado = entity.estado
            )
        }
    }

    override suspend fun actualizarPrestamo(prestamo: PrestamoModel) {
        prestamoDao.actualizarPrestamo(
            PrestamoEntity(
                id = prestamo.id,
                nombrePersona = prestamo.nombrePersona,
                cedulaPersona = prestamo.cedulaPersona,
                fechaPrestamo = prestamo.fechaPrestamo,
                valorPrestado = prestamo.valorPrestado,
                saldoPendiente = prestamo.saldoPendiente,
                interes = prestamo.interes,
                observacion = prestamo.observacion,
                fechaVencimiento = prestamo.fechaVencimiento,
                estado = prestamo.estado
            )
        )
    }

    override fun totalPrestadoPendiente(): Flow<Double?> {
        return prestamoDao.totalPrestadoPendiente()
    }
}