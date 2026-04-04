//package com.ethandev.cafecontable.domain.usecase
//
//import com.ethandev.cafecontable.data.local.dao.InventarioDao
//import com.ethandev.cafecontable.data.local.dao.MezclaDao
//import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
//import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
//import com.ethandev.cafecontable.data.local.entity.MezclaEntity
//import com.ethandev.cafecontable.domain.model.RegistrarMezclaInput
//import java.util.UUID
//
//class RegistrarMezclaUseCase(
//    private val mezclaDao: MezclaDao,
//    private val inventarioDao: InventarioDao
//) {
//
//    suspend operator fun invoke(input: RegistrarMezclaInput) {
//        require(input.items.isNotEmpty()) {
//            "Debes agregar al menos un item a la mezcla"
//        }
//
//        input.items.forEach { item ->
//            require(item.compraId.isNotBlank()) { "La compra es obligatoria" }
//            require(item.productoId.isNotBlank()) { "El producto es obligatorio" }
//            require(item.productoNombre.isNotBlank()) { "El nombre del producto es obligatorio" }
//            require(item.cantidadUsada > 0.0) { "La cantidad usada debe ser mayor a cero" }
//            require(item.costoUnitCompra >= 0L) { "El costo unitario no puede ser negativo" }
//        }
//
//        val cantidadTotal = input.items.sumOf { it.cantidadUsada }
//        val costoTotal = input.items.sumOf { it.cantidadUsada * it.costoUnitCompra.toDouble() }.toLong()
//
//        require(cantidadTotal > 0.0) {
//            "La cantidad total de la mezcla debe ser mayor a cero"
//        }
//
//        val costoPromedioKg = (costoTotal / cantidadTotal).toLong()
//        val mezclaId = UUID.randomUUID().toString()
//
//        val mezcla = MezclaEntity(
//            id = mezclaId,
//            fecha = input.fecha,
//            cantidadTotal = cantidadTotal,
//            cantidadDisponible = cantidadTotal,
//            costoTotal = costoTotal,
//            costoPromedioKg = costoPromedioKg,
//            estado = "DISPONIBLE",
//            nota = input.nota?.trim()?.ifBlank { null }
//        )
//
//        val detalles = input.items.map { item ->
//            MezclaDetalleEntity(
//                id = UUID.randomUUID().toString(),
//                mezclaId = mezclaId,
//                compraId = item.compraId,
//                productoId = item.productoId,
//                productoNombre = item.productoNombre,
//                cantidadUsada = item.cantidadUsada,
//                costoUnitCompra = item.costoUnitCompra,
//                subtotal = (item.cantidadUsada * item.costoUnitCompra.toDouble()).toLong()
//            )
//        }
//
//        mezclaDao.insertMezcla(mezcla)
//        mezclaDao.insertDetalles(detalles)
//
//        input.items.forEach { item ->
//            inventarioDao.insertMov(
//                KardexMovimientoEntity(
//                    id = UUID.randomUUID().toString(),
//                    fecha = input.fecha,
//                    productoId = item.productoId,
//                    tipo = "SALIDA",
//                    cantidad = item.cantidadUsada,
//                    costoUnit = item.costoUnitCompra,
//                    docTipo = "MEZCLA",
//                    docId = mezclaId,
//                    nota = "Salida por creación de mezcla"
//                )
//            )
//        }
//    }
//}

package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.RegistrarMezclaInput
import com.ethandev.cafecontable.domain.repository.MezclaRepository

class RegistrarMezclaUseCase(
    private val repository: MezclaRepository
) {
    suspend operator fun invoke(input: RegistrarMezclaInput) {
        repository.registrarMezcla(input)
    }
}