package com.ethandev.cafecontable.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ethandev.cafecontable.ui.screen.asignacionmezcla.AsignacionMezclaPedidoScreen
import com.ethandev.cafecontable.ui.screen.asignacionmezcla.AsignacionMezclaPedidoViewModel
import com.ethandev.cafecontable.ui.screen.compras.CompraCafeScreen
import com.ethandev.cafecontable.ui.screen.compras.CompraCafeViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporcobrar.CuentasPorCobrarScreen
import com.ethandev.cafecontable.ui.screen.cuentasporcobrar.CuentasPorCobrarViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporpagar.CuentasPorPagarScreen
import com.ethandev.cafecontable.ui.screen.cuentasporpagar.CuentasPorPagarViewModel
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasScreen
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel
import com.ethandev.cafecontable.ui.screen.historialventas.HistorialVentasScreen
import com.ethandev.cafecontable.ui.screen.historialventas.HistorialVentasViewModel
import com.ethandev.cafecontable.ui.screen.home.HomeScreen
import com.ethandev.cafecontable.ui.screen.inventario.InventarioScreen
import com.ethandev.cafecontable.ui.screen.inventario.InventarioViewModel
import com.ethandev.cafecontable.ui.screen.liquidacion.LiquidacionScreen
import com.ethandev.cafecontable.ui.screen.liquidacion.LiquidacionViewModel
import com.ethandev.cafecontable.ui.screen.mezcla.MezclasScreen
import com.ethandev.cafecontable.ui.screen.mezcla.MezclasViewModel
import com.ethandev.cafecontable.ui.screen.operacion.OperacionScreen
import com.ethandev.cafecontable.ui.screen.operacion.OperacionViewModel
import com.ethandev.cafecontable.ui.screen.preparacionentrega.PreparacionEntregaScreen
import com.ethandev.cafecontable.ui.screen.preparacionentrega.PreparacionEntregaViewModel
import com.ethandev.cafecontable.ui.screen.prestamos.ConsultaPrestamosScreen
import com.ethandev.cafecontable.ui.screen.prestamos.PrestamosScreen
import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeScreen
import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeViewModel
import com.ethandev.cafecontable.ui.screen.ventaspedido.VentasPedidoScreen
import com.ethandev.cafecontable.ui.screen.ventaspedido.VentasPedidoViewModel
import com.ethandev.cafecontable.ui.viewmodel.PrestamosViewModel

@Composable
fun AppNavGraph(
    compraVm: CompraCafeViewModel,
    ventaVm: VentaCafeViewModel,
    inventarioVm: InventarioViewModel,
    historialComprasVm: HistorialComprasViewModel,
    historialVentasVm: HistorialVentasViewModel,
    cuentasPorCobrarVm: CuentasPorCobrarViewModel,
    cuentasPorPagarVm: CuentasPorPagarViewModel,
    prestamosVm: PrestamosViewModel,
    ventasPedidoVm: VentasPedidoViewModel,
    preparacionEntregaVm: PreparacionEntregaViewModel,
    mezclaVm : MezclasViewModel,
    asignacionMezclaPedidoVm : AsignacionMezclaPedidoViewModel,
    liquidacionVm : LiquidacionViewModel,
    operacionesgastosVm: OperacionViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val title = when (currentRoute) {
        Routes.COMPRAS -> "Compras"
        Routes.VENTAS -> "Ventas"
        Routes.VENTAS_PEDIDO -> "Ventas / Pedidos"
        Routes.PREPARACION_ENTREGA_ARG,
        Routes.PREPARACION_ENTREGA -> "Preparación de Mezcla"
        Routes.INVENTARIO -> "Inventario"
        Routes.HISTORIAL_COMPRA -> "Historial Compras"
        Routes.HISTORIAL_VENTA -> "Historial Ventas"
        Routes.CUENTAS_POR_COBRAR -> "Cuentas Por Cobrar"
        Routes.CUENTAS_POR_PAGAR -> "Cuentas Por Pagar"
        Routes.PRESTAMOS -> "Prestamos"
        Routes.CONSULTA_PRESTAMOS -> "Consulta Prestamos"
        Routes.ROUTE_LIQUIDACION -> "Liquidacion"
        Routes.ROUTE_OPERACION_GASTOS -> "operaciones y gastos"
        else -> "Inicio"
    }

    val showBack = currentRoute == Routes.HISTORIAL_COMPRA ||
            currentRoute == Routes.HISTORIAL_VENTA ||
            currentRoute == Routes.CUENTAS_POR_COBRAR ||
            currentRoute == Routes.CUENTAS_POR_PAGAR ||
            currentRoute == Routes.PRESTAMOS ||
            currentRoute == Routes.CONSULTA_PRESTAMOS ||
            currentRoute == Routes.VENTAS_PEDIDO ||
            currentRoute == Routes.PREPARACION_ENTREGA_ARG ||
            currentRoute == Routes.MEZCLAS ||
            currentRoute == Routes.ASIGNACION_MEZCLA_PEDIDO ||
            currentRoute == Routes.ROUTE_LIQUIDACION ||
            currentRoute == Routes.ROUTE_OPERACION_GASTOS ||


            currentRoute?.startsWith("${Routes.PREPARACION_ENTREGA}/") == true

    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
                showBack = showBack,
                onBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    innerPadding = innerPadding,
                    onGoCompras = { navController.navigate(Routes.COMPRAS) },
                    onGoVentas = { navController.navigate(Routes.VENTAS) },
                    onGoVentasPedido = { navController.navigate(Routes.VENTAS_PEDIDO) },
                    onGoInventario = { navController.navigate(Routes.INVENTARIO) },
                    onGoHistorialCompras = { navController.navigate(Routes.HISTORIAL_COMPRA) },
                    onGoHistorialVentas = { navController.navigate(Routes.HISTORIAL_VENTA) },
                    onGoCuentasPorCobrar = { navController.navigate(Routes.CUENTAS_POR_COBRAR) },
                    onGoCuentasPorPagar = { navController.navigate(Routes.CUENTAS_POR_PAGAR) },
                    onGoPrestamos = { navController.navigate(Routes.PRESTAMOS) },
                    onGoMezclas  = { navController.navigate(Routes.MEZCLAS) },
                    onGoAsignacionMezclaPedido  = { navController.navigate(Routes.ASIGNACION_MEZCLA_PEDIDO) },
                    onGoLiquidacion  = { navController.navigate(Routes.ROUTE_LIQUIDACION) },
                    onGoOperacionesGastos  = { navController.navigate(Routes.ROUTE_OPERACION_GASTOS) }



                )
            }

            composable(Routes.COMPRAS) {
                CompraCafeScreen(compraVm)
            }

            composable(Routes.VENTAS) {
                VentaCafeScreen(ventaVm)
            }

            composable(Routes.VENTAS_PEDIDO) {
                VentasPedidoScreen(
                    vm = ventasPedidoVm,
                    onIrAsignacionMezcla = { ventaId ->
                        navController.navigate(Routes.asignacionMezclaRoute(ventaId))
                    }
                )
            }

            composable(Routes.MEZCLAS) {
                MezclasScreen(mezclaVm)
            }

            composable(Routes.ASIGNACION_MEZCLA_PEDIDO) {
                AsignacionMezclaPedidoScreen(asignacionMezclaPedidoVm)
            }

            composable(
                route = Routes.PREPARACION_ENTREGA_ARG,
                arguments = listOf(
                    navArgument("ventaId") {
                        type = NavType.StringType
                    }
                )
            ) { backStack ->
                val ventaId = backStack.arguments?.getString("ventaId").orEmpty()

                PreparacionEntregaScreen(
                    ventaId = ventaId,
                    vm = preparacionEntregaVm
                )
            }

            composable(Routes.INVENTARIO) {
                InventarioScreen(inventarioVm)
            }

            composable(Routes.HISTORIAL_COMPRA) {
                HistorialComprasScreen(historialComprasVm)
            }

            composable(Routes.HISTORIAL_VENTA) {
                HistorialVentasScreen(historialVentasVm)
            }

            composable(Routes.CUENTAS_POR_COBRAR) {
                CuentasPorCobrarScreen(cuentasPorCobrarVm)
            }

            composable(Routes.CUENTAS_POR_PAGAR) {
                CuentasPorPagarScreen(cuentasPorPagarVm)
            }

            composable(Routes.PRESTAMOS) {
                PrestamosScreen(navController, prestamosVm, innerPadding)
            }

            composable(Routes.CONSULTA_PRESTAMOS) {
                ConsultaPrestamosScreen(prestamosVm, innerPadding)
            }

            composable(
                route = "asignacion_mezcla/{ventaId}"
            ) { backStackEntry ->
                val ventaId = backStackEntry.arguments?.getString("ventaId") ?: ""

                AsignacionMezclaPedidoScreen(
                   // ventaId = ventaId
                   asignacionMezclaPedidoVm
                )
            }

            composable(Routes.ROUTE_LIQUIDACION) {
                LiquidacionScreen(liquidacionVm)

            }

            composable(Routes.ROUTE_OPERACION_GASTOS) {
                OperacionScreen(operacionesgastosVm)

            }



        }
    }
}