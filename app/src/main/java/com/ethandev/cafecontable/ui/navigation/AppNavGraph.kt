//package com.ethandev.cafecontable.ui.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.ethandev.cafecontable.ui.screen.compras.CompraCafeScreen
//import com.ethandev.cafecontable.ui.screen.compras.CompraCafeViewModel
//import com.ethandev.cafecontable.ui.screen.home.HomeScreen
//import com.ethandev.cafecontable.ui.screen.inventario.InventarioScreen
//import com.ethandev.cafecontable.ui.screen.inventario.InventarioViewModel
//import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeScreen
//import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeViewModel
//
//@Composable
//fun AppNavGraph(
//    compraVm: CompraCafeViewModel,
//    ventaVm: VentaCafeViewModel,
//    inventarioVm: InventarioViewModel
//) {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = Routes.HOME
//    ) {
//        composable(Routes.HOME) {
//            HomeScreen(
//                onGoCompras = { navController.navigate(Routes.COMPRAS) },
//                onGoVentas = { navController.navigate(Routes.VENTAS) },
//                onGoInventario = { navController.navigate(Routes.INVENTARIO) }
//            )
//        }
//
//        composable(Routes.COMPRAS) {
//            CompraCafeScreen(compraVm)
//        }
//
//        composable(Routes.VENTAS) {
//            VentaCafeScreen(ventaVm)
//        }
//
//        composable(Routes.INVENTARIO) {
//            InventarioScreen(inventarioVm)
//        }
//    }
//}
package com.ethandev.cafecontable.ui.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.internal.composableLambda
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ethandev.cafecontable.domain.model.CuentaPorCobrarModel
import com.ethandev.cafecontable.ui.screen.compras.CompraCafeScreen
import com.ethandev.cafecontable.ui.screen.compras.CompraCafeViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporcobrar.CuentasPorCobrarScreen
import com.ethandev.cafecontable.ui.screen.cuentasporcobrar.CuentasPorCobrarViewModel
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasScreen
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel
import com.ethandev.cafecontable.ui.screen.historialventas.HistorialVentasScreen
import com.ethandev.cafecontable.ui.screen.historialventas.HistorialVentasViewModel
import com.ethandev.cafecontable.ui.screen.home.HomeScreen
import com.ethandev.cafecontable.ui.screen.inventario.InventarioScreen
import com.ethandev.cafecontable.ui.screen.inventario.InventarioViewModel
import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeScreen
import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeViewModel

@Composable
fun AppNavGraph(
    compraVm: CompraCafeViewModel,
    ventaVm: VentaCafeViewModel,
    inventarioVm: InventarioViewModel,
    historialComprasVm: HistorialComprasViewModel,
    historialVentasVm: HistorialVentasViewModel,
    cuentasPorCobrarVm: CuentasPorCobrarViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val title = when (currentRoute) {
        Routes.COMPRAS -> "Compras"
        Routes.VENTAS -> "Ventas"
        Routes.INVENTARIO -> "Inventario"
        Routes.HISTORIAL_COMPRA -> "Historial Compras"
        Routes.HISTORIAL_VENTA -> "Historial Ventas"
        Routes.CUENTAS_POR_COBRAR -> "Cuentas Por Cobrar"
        else -> "Inicio"
    }

   // val showBack = currentRoute != Routes.HOME

    val showBack = currentRoute == Routes.HISTORIAL_COMPRA ||
            currentRoute == Routes.HISTORIAL_VENTA ||
            currentRoute == Routes.CUENTAS_POR_COBRAR

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
            startDestination = Routes.HOME
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    innerPadding = innerPadding,
                    onGoCompras = { navController.navigate(Routes.COMPRAS) },
                    onGoVentas = { navController.navigate(Routes.VENTAS) },
                    onGoInventario = { navController.navigate(Routes.INVENTARIO) },
                    onGoHistorialCompras = {navController.navigate(Routes.HISTORIAL_COMPRA)},
                    onGoHistorialVentas = { navController.navigate(Routes.HISTORIAL_VENTA)},
                    onGoCuentasPorCobrar = {navController.navigate(Routes.CUENTAS_POR_COBRAR)}
                )
            }

            composable(Routes.COMPRAS) {
                CompraCafeScreen(compraVm)
            }

            composable(Routes.VENTAS) {
                VentaCafeScreen(ventaVm)
            }

            composable(Routes.INVENTARIO) {
                InventarioScreen(inventarioVm)
            }

            composable(Routes.HISTORIAL_COMPRA){
                HistorialComprasScreen(historialComprasVm)
            }

            composable(Routes.HISTORIAL_VENTA){
                HistorialVentasScreen(historialVentasVm)
            }

            composable(Routes.CUENTAS_POR_COBRAR){
                CuentasPorCobrarScreen(cuentasPorCobrarVm)
            }

        }
    }
}
