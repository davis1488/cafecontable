package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.AbonoCuentaPorCobrarModel
import com.ethandev.cafecontable.domain.model.CuentaPorCobrarModel
import com.ethandev.cafecontable.domain.model.RegistrarAbonoInput
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPagarInput


interface CuentaPorCobrarRepository {
    suspend fun listarPendientes(): List<CuentaPorCobrarModel>
    suspend fun registrarAbono(input: RegistrarAbonoInput)
    suspend fun listarAbonos(cuentaId:String): List<AbonoCuentaPorCobrarModel>


}

