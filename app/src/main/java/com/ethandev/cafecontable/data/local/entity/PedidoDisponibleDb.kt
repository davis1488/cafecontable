package com.ethandev.cafecontable.data.local.entity

data class PedidoDisponibleDb(
    val pedidoId: String,
    val clienteNombre: String,
    val productoNombre: String,
    val cantidadPedido: Double,
    val cantidadAsignada: Double,
    val precioUnitVenta : Long
    //val cantidadTotal: Double,


    )