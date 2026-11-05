//package com.ethandev.cafecontable.ui.screen.inventario
//
//import com.ethandev.cafecontable.domain.model.InventarioItemModel
//import com.ethandev.cafecontable.domain.model.MovimientoInventarioModel
//
//data class InventarioState(
//    val loading: Boolean = false,
//    val items: List<InventarioItemModel> = emptyList(),
//    val movimientos: List<MovimientoInventarioModel> = emptyList(),
//    val error: String? = null
//)
package com.ethandev.cafecontable.ui.screen.inventario

import com.ethandev.cafecontable.domain.model.InventarioItemModel
import com.ethandev.cafecontable.domain.model.MovimientoInventarioModel

data class InventarioState(
    val loading: Boolean = false,
    val items: List<InventarioItemModel> = emptyList(),
    val movimientos: List<MovimientoInventarioModel> = emptyList(),
    val error: String? = null
)
