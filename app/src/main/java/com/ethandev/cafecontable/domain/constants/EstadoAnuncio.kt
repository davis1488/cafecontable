package com.ethandev.cafecontable.domain.constants


enum class EstadoAnuncio(val value: String) {
    CREADO("CREADO"),
    ENTREGA_PARCIAL("ENTREGA_PARCIAL"),
    ENTREGA_TOTAL("ENTREGA_TOTAL");

    companion object {
        fun from(value: String): EstadoAnuncio {
            return entries.firstOrNull { it.value == value } ?: CREADO
        }
    }
}

//fun calcularEstadoAnuncio(
//    cantidadAsignada: Double,
//    cantidadPactada: Double
//): EstadoAnuncio {
//    return when {
//        cantidadAsignada <= 0.0 -> EstadoAnuncio.CREADO
//        cantidadAsignada < cantidadPactada -> EstadoAnuncio.ENTREGA_PARCIAL
//        else -> EstadoAnuncio.ENTREGA_TOTAL
//    }
//}