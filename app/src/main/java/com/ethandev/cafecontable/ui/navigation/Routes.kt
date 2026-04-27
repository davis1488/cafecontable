package com.ethandev.cafecontable.ui.navigation

object Routes {
    const val HOME = "home"
    const val COMPRAS = "compras"
    const val VENTAS = "ventas"
    const val VENTAS_PEDIDO = "ventas_pedido"
    const val PREPARACION_ENTREGA = "preparacion_entrega"
    const val PREPARACION_ENTREGA_ARG = "preparacion_entrega/{ventaId}"
    const val INVENTARIO = "inventario"
    const val HISTORIAL_COMPRA = "historial_compra"
    const val HISTORIAL_VENTA = "historial_venta"
    const val CUENTAS_POR_COBRAR = "cuentas_por_cobrar"
    const val CUENTAS_POR_PAGAR = "cuentas_por_pagar"
    const val PRESTAMOS = "prestamos"
    const val CONSULTA_PRESTAMOS = "consulta_prestamos"
    const val MEZCLAS = "mezclas"
    const val ASIGNACION_MEZCLA_PEDIDO = "asignacion_mezcla_pedido"
    const val ROUTE_LIQUIDACION = "liquidacion"
    const val ROUTE_OPERACION_GASTOS = "operacion_gastos"
    const val ROUTE_UTILIDAD = "utilidad"


    fun asignacionMezclaRoute(ventaId: String) =
        "asignacion_mezcla/$ventaId"
}
