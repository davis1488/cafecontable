//package com.ethandev.cafecontable.domain.repository
//
//class ProductoRepository {
//}


package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.Producto

interface ProductoRepository {
    suspend fun crear(nombre: String,   unidad: String, precioVenta: Long)
    suspend fun listar(): List<Producto>
}