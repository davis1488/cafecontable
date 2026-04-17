package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.GastoOperacionEntity
import com.ethandev.cafecontable.domain.model.RegistrarGastoOperacionInput
import java.util.UUID

class RegistrarGastoOperacionUseCase(
    private val db: AppDatabase
) {
    suspend operator fun invoke(input: RegistrarGastoOperacionInput) {

        if (input.valor <= 0) {
            throw IllegalStateException("Valor inválido")
        }

        db.gastoOperacionDao().insert(
            GastoOperacionEntity(
                id = UUID.randomUUID().toString(),
                operacionId = input.operacionId,
                fecha = System.currentTimeMillis(),
                categoria = input.categoria,
                descripcion = input.descripcion,
                valor = input.valor,
                tercero = input.tercero,
                observacion = input.observacion
            )
        )
    }
}