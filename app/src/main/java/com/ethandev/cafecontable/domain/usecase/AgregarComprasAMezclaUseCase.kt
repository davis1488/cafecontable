package com.ethandev.cafecontable.domain.usecase

import androidx.room.withTransaction
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
import com.ethandev.cafecontable.domain.constants.EstadoMezcla
import com.ethandev.cafecontable.domain.model.AgregarComprasAMezclaInput
import java.util.UUID

class AgregarComprasAMezclaUseCase(
    private val db: AppDatabase
) {

    suspend operator fun invoke(input: AgregarComprasAMezclaInput) {
        db.withTransaction {
            val mezclaDao = db.mezclaDao()
            val compraDao = db.compraDao()

            val mezcla = mezclaDao.obtenerMezclaPorId(input.mezclaId)
                ?: throw IllegalArgumentException("La mezcla no existe")

            if (!mezcla.estado.equals(EstadoMezcla.CREADO.name, ignoreCase = true)) {
                throw IllegalStateException("Solo se pueden agregar compras a mezclas en estado CREADO")
            }

            if (input.items.isEmpty()) {
                throw IllegalArgumentException("Debes enviar al menos un item")
            }

            val comprasDisponibles = compraDao.obtenerComprasDisponiblesParaMezcla()
                .associateBy { it.compraId }

            input.items.forEach { item ->
                val compraDisponible = comprasDisponibles[item.compraId]
                    ?: throw IllegalArgumentException("La compra ${item.compraId} no está disponible para mezcla")

                if (item.cantidadUsada <= 0.0) {
                    throw IllegalArgumentException("La cantidad usada debe ser mayor a cero")
                }

                if (item.cantidadUsada > compraDisponible.cantidadDisponible) {
                    throw IllegalArgumentException(
                        "La cantidad usada para la compra ${item.compraId} supera la disponible"
                    )
                }

                val yaExiste = mezclaDao.existeCompraEnMezcla(
                    mezclaId = input.mezclaId,
                    compraId = item.compraId
                ) > 0

                if (yaExiste) {
                    throw IllegalArgumentException(
                        "La compra ${item.compraId} ya fue agregada a esta mezcla"
                    )
                }
            }

            val nuevosDetalles = input.items.map { item ->
                MezclaDetalleEntity(
                    id = UUID.randomUUID().toString(),
                    mezclaId = input.mezclaId,
                    compraId = item.compraId,
                    productoId = item.productoId,
                    productoNombre = item.productoNombre,
                    cantidadUsada = item.cantidadUsada,
                    costoUnitCompra = item.costoUnitCompra,
                   // subtotal = item.subtotal
                )
            }

            mezclaDao.insertarDetallesMezcla(nuevosDetalles)

            val todosLosDetalles = mezclaDao.obtenerDetallesPorMezclaId(input.mezclaId)

            val nuevaCantidadTotal = todosLosDetalles.sumOf { it.cantidadUsada }
            val nuevoCostoTotal = todosLosDetalles.sumOf {
                (it.cantidadUsada * it.costoUnitCompra.toDouble()).toLong()
            }
            val nuevoCostoPromedioKg = if (nuevaCantidadTotal > 0.0) {
                (nuevoCostoTotal / nuevaCantidadTotal).toLong()
            } else {
                0L
            }

            mezclaDao.actualizarTotalesMezcla(
                mezclaId = input.mezclaId,
                cantidadTotal = nuevaCantidadTotal,
                costoTotal = nuevoCostoTotal,
                costoPromedioKg = nuevoCostoPromedioKg
            )
        }
    }
}