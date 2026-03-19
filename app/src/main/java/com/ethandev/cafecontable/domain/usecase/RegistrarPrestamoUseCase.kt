package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.PrestamoModel
import com.ethandev.cafecontable.domain.model.RegistrarPrestamoInput
import com.ethandev.cafecontable.domain.repository.PrestamoRepository

class RegistrarPrestamoUseCase(
    private val repository: PrestamoRepository
) {
    suspend operator fun invoke(input: RegistrarPrestamoInput) {
        if (input.nombrePersona.isBlank()) {
            throw IllegalArgumentException("El nombre es obligatorio")
        }

        if (input.cedulaPersona.isBlank()) {
            throw IllegalArgumentException("La cédula es obligatoria")
        }

        if (input.valorPrestado <= 0.0) {
            throw IllegalArgumentException("El valor prestado debe ser mayor a 0")
        }

        val prestamo = PrestamoModel(
            nombrePersona = input.nombrePersona,
            cedulaPersona = input.cedulaPersona,
            fechaPrestamo = input.fechaPrestamo,
            valorPrestado = input.valorPrestado,
            saldoPendiente = input.valorPrestado + input.interes,
            interes = input.interes,
            observacion = input.observacion,
            fechaVencimiento = input.fechaVencimiento,
            estado = "PENDIENTE"
        )

        repository.registrarPrestamo(prestamo)
    }
}