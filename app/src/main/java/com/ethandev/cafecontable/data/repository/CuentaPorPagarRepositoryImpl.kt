package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.local.dao.CuentaPorPagarDao
import com.ethandev.cafecontable.data.local.entity.AbonoCuentaPorPagarEntity
import com.ethandev.cafecontable.domain.model.AbonoCuentaPorPagarModel
import com.ethandev.cafecontable.domain.model.CuentaPorPagarModel
import com.ethandev.cafecontable.domain.model.RegistrarAbonoInput
import com.ethandev.cafecontable.domain.repository.CuentaPorPagarRepository
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPagarInput
import java.util.UUID

class CuentaPorPagarRepositoryImpl(
    private val dao: CuentaPorPagarDao
) : CuentaPorPagarRepository {

    override suspend fun listarPendientes(): List<CuentaPorPagarModel> {
        return dao.listarPendientes().map {
            CuentaPorPagarModel(
                id = it.id,
                fecha = it.fecha,
                compraId = it.compraId,
                proveedor = it.proveedor,
                valorInicial = it.valorInicial,
                saldoPendiente = it.saldoPendiente,
                estado = it.estado,
                nota = it.nota
            )
        }
    }

    override suspend fun registrarAbono(input: RegistrarAbonoInput) {
        val cuenta = dao.getById(input.cuentaId)
            ?: throw IllegalStateException("La cuenta por pagar no existe")

        if (input.valor <= 0) {
            throw IllegalStateException("El valor del abono debe ser mayor a 0")
        }

        if (input.valor > cuenta.saldoPendiente) {
            throw IllegalStateException("El abono no puede ser mayor al saldo pendiente")
        }

        dao.insertAbono(
            AbonoCuentaPorPagarEntity(
                id = UUID.randomUUID().toString(),
                cuentaId = input.cuentaId,
                fecha = System.currentTimeMillis(),
                valor = input.valor,
                nota = input.nota?.trim()?.ifBlank { null }
            )
        )

        val nuevoSaldo = cuenta.saldoPendiente - input.valor
        val nuevoEstado = if (nuevoSaldo == 0L) "PAGADO" else "PENDIENTE"

        dao.update(
            cuenta.copy(
                saldoPendiente = nuevoSaldo,
                estado = nuevoEstado
            )
        )
    }

    override suspend fun listarAbonos(cuentaId: String): List<AbonoCuentaPorPagarModel> {
        return dao.listarAbonos(cuentaId).map {
            AbonoCuentaPorPagarModel(
                id = it.id,
                cuentaId = it.cuentaId,
                fecha = it.fecha,
                valor = it.valor,
                nota = it.nota
            )
        }
    }
}