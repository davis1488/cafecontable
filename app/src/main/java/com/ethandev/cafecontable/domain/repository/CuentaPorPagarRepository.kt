package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.AbonoCuentaPorPagarModel
import com.ethandev.cafecontable.domain.model.CuentaPorPagarModel
import com.ethandev.cafecontable.domain.model.RegistrarAbonoInput
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPagarInput

interface CuentaPorPagarRepository {
    suspend fun listarPendientes() : List<CuentaPorPagarModel>
    suspend fun registrarAbono(input: RegistrarAbonoInput)
    suspend fun listarAbonos(cuentaId:String): List<AbonoCuentaPorPagarModel>
}