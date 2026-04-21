package com.ethandev.cafecontable.domain.constants

enum class EstadoAnuncio(val valorDb: String, val label: String) {

    CREADO("CREADO", "Creado"),
    ENTREGA_PARCIAL("ENTREGA_PARCIAL", "Entrega parcial"),
    ENTREGA_TOTAL("ENTREGA_TOTAL", "Entrega total"),
    LIQUIDADO("LIQUIDADO", "Liquidado");

    companion object {

        fun from(valor: String?): EstadoAnuncio {
            return entries.firstOrNull {
                it.valorDb.equals(valor, ignoreCase = true)
            } ?: CREADO
        }

        val TODOS = entries.toList()

        val VALORES_DB = entries.map { it.valorDb }
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