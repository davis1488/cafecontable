package com.ethandev.cafecontable.domain.constants

enum class EstadoMezcla(val value: String) {
    CREADO("CREADO"),
    PENDIENTE_ENTREGA("PENDIENTE_ENTREGA"),
    ENTREGADO("ENTREGADO"),
    ANALIZADO("ANALIZADO");

    companion object {
        fun from(value: String): EstadoMezcla {
            return entries.firstOrNull { it.value == value } ?: CREADO
        }

        val PARA_ASIGNACION = listOf(
            ENTREGADO,
            ANALIZADO
        )

        val TODOS = listOf(
            CREADO,
            PENDIENTE_ENTREGA,
            ENTREGADO,
            ANALIZADO

        )
    }
}