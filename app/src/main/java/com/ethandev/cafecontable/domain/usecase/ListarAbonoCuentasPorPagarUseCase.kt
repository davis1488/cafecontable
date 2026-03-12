
package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.AbonoCuentaPorPagarModel
import com.ethandev.cafecontable.domain.repository.CuentaPorPagarRepository

class ListarAbonosCuentaPorPagarUseCase(
    private val repo: CuentaPorPagarRepository
) {
    suspend operator fun invoke(cuentaId: String): List<AbonoCuentaPorPagarModel> {
        return repo.listarAbonos(cuentaId)
    }
}