//package com.ethandev.cafecontable.domain.usecase
//
//class CrearProductoUseCase {
//}


package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.Producto
import com.ethandev.cafecontable.domain.repository.ProductoRepository


class CrearProductoUseCase(private val repo: ProductoRepository) {
    suspend operator fun invoke(nombre: String, unidad: String, precio: Long) {
        repo.crear(nombre, unidad, precio)
    }
}