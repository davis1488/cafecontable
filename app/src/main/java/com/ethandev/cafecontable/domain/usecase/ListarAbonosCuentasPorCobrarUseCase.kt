package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.AbonoCuentaPorCobrarModel
import com.ethandev.cafecontable.domain.repository.CuentaPorCobrarRepository

class ListarAbonosCuentasPorCobrarUseCase (
    private val repo: CuentaPorCobrarRepository
    ){
    suspend operator fun invoke(cuentaId:String): List<AbonoCuentaPorCobrarModel>{
        return repo.listarAbonos(cuentaId)
    }

}