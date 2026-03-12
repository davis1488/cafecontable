
package com.ethandev.cafecontable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.ui.navigation.AppNavGraph
import com.ethandev.cafecontable.ui.screen.compras.CompraCafeViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporcobrar.CuentasPorCobrarViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporpagar.CuentasPorPagarViewModel
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel
import com.ethandev.cafecontable.ui.screen.historialventas.HistorialVentasViewModel
import com.ethandev.cafecontable.ui.screen.inventario.InventarioViewModel
import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeViewModel
import com.ethandev.cafecontable.ui.theme.CafecontableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val compraVm = CompraCafeViewModel(
            AppModule.provideCompraUseCase(this)
        )

        val ventaVm = VentaCafeViewModel(
            AppModule.provideVentaUseCase(this)
        )

        val (_, _, listarInventarioUseCase) = AppModule.provideInventarioUseCases(this)
        val inventarioVm = InventarioViewModel(
            listarInventarioUseCase
        )

        val historialComprasVm = HistorialComprasViewModel(
            AppModule.provideHistorialCompraUseCase(this)
        )

        val historialVentasVm = HistorialVentasViewModel(
            AppModule.provideHistorialVentaUseCase(this)
        )

        val cuentasPorCobrarVm = CuentasPorCobrarViewModel(
            AppModule.provideCuentaPorCobrarUseCase(this),
            AppModule.provideRegistrarAbonoCuentaPorCobrarUseCase(this),
            AppModule.provideListarAbonosCuentasPorCobrarUseCase(this)
        )

        val cuentasPorPagarVm = CuentasPorPagarViewModel(
            AppModule.provideCuentaPorPagarUseCase(this),
            AppModule.provideRegistrarAbonoCuentaPorPagarUseCase(this),
            AppModule.provideListarAbonosCuentaPorPagarUseCase(this)
        )



        setContent {
            CafecontableTheme {
                AppNavGraph(
                    compraVm = compraVm,
                    ventaVm = ventaVm,
                    inventarioVm = inventarioVm,
                    historialComprasVm = historialComprasVm,
                    historialVentasVm = historialVentasVm,
                    cuentasPorCobrarVm = cuentasPorCobrarVm,
                    cuentasPorPagarVm = cuentasPorPagarVm

                )
            }
        }
    }
}