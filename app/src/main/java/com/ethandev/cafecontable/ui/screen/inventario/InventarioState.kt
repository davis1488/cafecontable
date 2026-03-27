package com.ethandev.cafecontable.ui.screen.inventario

import com.ethandev.cafecontable.domain.model.InventarioItemModel

data class InventarioState(
    val loading: Boolean = false,
    val items: List<InventarioItemModel> = emptyList(),
    val error: String? = null
)