package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.CompraCafeImput

//data class CompraCafeImput(
//    val productoNombre: String,  // "Cafe" | "Pasilla"
//    val unidad: String,          // "KG" | "LB" | "ARROBA" (para guardar en producto si se crea)
//    val cantidad: Double,
//    val precioUnitCompra: Long,
//    val proveedor: String?,
//    val esCredito: Boolean,
//    val nota: String?,
//    val abono:Long,
//    val operacionCompraId: String?
//
//)

interface CompraRepository {
    suspend fun registrarCompra(imput: CompraCafeImput)
}