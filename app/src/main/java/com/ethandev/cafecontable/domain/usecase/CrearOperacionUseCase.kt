package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.OperacionDao
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.model.CrearOperacionInput
import java.util.UUID

class CrearOperacionUseCase(
    private val db: AppDatabase
) {
    suspend operator fun invoke(input: CrearOperacionInput) {
        if (input.nombre.isBlank()) {
            throw IllegalStateException("Debe ingresar un nombre")
        }

        db.operacionDao().insert(
            OperacionEntity(
                id = UUID.randomUUID().toString(),
                fecha = System.currentTimeMillis(),
                tipo = input.tipo,
                nombre = input.nombre,
                descripcion = input.descripcion
            )
        )
    }
}