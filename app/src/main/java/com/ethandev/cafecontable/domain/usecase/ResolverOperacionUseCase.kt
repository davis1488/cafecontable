package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.dao.OperacionDao
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.constants.OPERACION_NUEVA_ID
import com.ethandev.cafecontable.domain.model.CrearOperacionInput
import java.util.UUID

class ResolverOperacionUseCase(
    private val operacionDao: OperacionDao
) {
    suspend operator fun invoke(
        operacionIdSeleccionada: String?,
        tipo: String,
        nombreAutomatico: String,
        descripcionAutomatica: String? = null
    ): String {
        if (operacionIdSeleccionada.isNullOrBlank()) {
            throw IllegalStateException("Debe seleccionar una operación")
        }

        if (operacionIdSeleccionada != OPERACION_NUEVA_ID) {
            return operacionIdSeleccionada
        }

        val nuevaOperacion = OperacionEntity(
            id = UUID.randomUUID().toString(),
            fecha = System.currentTimeMillis(),
            tipo = tipo,
            nombre = nombreAutomatico,
            descripcion = descripcionAutomatica,
            estado = "ACTIVA"
        )

        operacionDao.insert(nuevaOperacion)

        return nuevaOperacion.id
    }
}