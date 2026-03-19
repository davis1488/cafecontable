package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.AbonoPrestamoModel
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPrestamoInput
import com.ethandev.cafecontable.domain.repository.PrestamoRepository

class RegistrarAbonoPrestamoUseCase(
    private val repository: PrestamoRepository
) {
    suspend operator fun invoke(input: RegistrarAbonoPrestamoInput) {
        val prestamo = repository.obtenerPrestamoPorId(input.prestamoId)
            ?: throw IllegalArgumentException("Préstamo no encontrado")

        if (input.valorAbono <= 0.0) {
            throw IllegalArgumentException("El abono debe ser mayor a 0")
        }

        if (input.valorAbono > prestamo.saldoPendiente) {
            throw IllegalArgumentException("El abono no puede ser mayor al saldo pendiente")
        }

        repository.registrarAbono(
            AbonoPrestamoModel(
                prestamoId = input.prestamoId,
                fechaAbono = input.fechaAbono,
                valorAbono = input.valorAbono,
                observacion = input.observacion
            )
        )

        val nuevoSaldo = prestamo.saldoPendiente - input.valorAbono

        repository.actualizarPrestamo(
            prestamo.copy(
                saldoPendiente = nuevoSaldo,
                estado = if (nuevoSaldo <= 0.0) "PAGADO" else "PENDIENTE"
            )
        )
    }
}