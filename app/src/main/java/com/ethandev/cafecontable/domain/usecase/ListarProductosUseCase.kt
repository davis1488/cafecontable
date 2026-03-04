//package com.ethandev.cafecontable.domain.usecase
//
//class ListarProductosUseCase {
//}


package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.model.Producto
import com.ethandev.cafecontable.domain.repository.ProductoRepository


class ListarProductosUseCase(private val repo: ProductoRepository) {
    suspend operator fun invoke(): List<Producto> = repo.listar()
}