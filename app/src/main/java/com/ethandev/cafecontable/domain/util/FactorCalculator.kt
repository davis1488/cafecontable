//package com.ethandev.cafecontable.domain.util
//
//class FactorCalculator {
//}


package com.ethandev.cafecontable.domain.util

import kotlin.math.abs

data class ResultadoFactor(
    val ajusteFactor: Long,
    val totalFinal: Long,
    val observacion: String,
    val porcentajeAplicado: Double
)

fun calcularAjustePorFactor(
    subtotalVenta: Long,
    factor: Double
): ResultadoFactor {
    val diferencia = factor - 90.0
    val porcentaje = abs(diferencia)
    val ajuste = (subtotalVenta * (porcentaje / 100.0)).toLong()

    return when {
        diferencia > 0 -> {
            ResultadoFactor(
                ajusteFactor = -ajuste,
                totalFinal = subtotalVenta - ajuste,
                observacion = "Descuento por factor mayor a 90",
                porcentajeAplicado = porcentaje
            )
        }
        diferencia < 0 -> {
            ResultadoFactor(
                ajusteFactor = ajuste,
                totalFinal = subtotalVenta + ajuste,
                observacion = "Bonificación por factor menor a 90",
                porcentajeAplicado = porcentaje
            )
        }
        else -> {
            ResultadoFactor(
                ajusteFactor = 0L,
                totalFinal = subtotalVenta,
                observacion = "Sin ajuste por factor",
                porcentajeAplicado = 0.0
            )
        }
    }
}