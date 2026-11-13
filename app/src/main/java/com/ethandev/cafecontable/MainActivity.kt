
package com.ethandev.cafecontable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.data.local.session.SessionManager
import com.ethandev.cafecontable.data.remote.AuthTokenManager
import com.ethandev.cafecontable.data.repository.PrestamoRepositoryImpl
import com.ethandev.cafecontable.domain.usecase.ActualizarCompraUseCase
import com.ethandev.cafecontable.domain.usecase.AnularCompraUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerCompraPorIdUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialComprasUseCase
import com.ethandev.cafecontable.ui.navigation.AppNavGraph
import com.ethandev.cafecontable.ui.screen.compras.CompraCafeViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporcobrar.CuentasPorCobrarViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporpagar.CuentasPorPagarViewModel
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel
import com.ethandev.cafecontable.ui.screen.historialventas.HistorialVentasViewModel
import com.ethandev.cafecontable.ui.screen.inventario.InventarioViewModel
import com.ethandev.cafecontable.ui.screen.login.LoginScreen
import com.ethandev.cafecontable.ui.screen.prestamos.PrestamosViewModel
import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeViewModel
import com.ethandev.cafecontable.ui.theme.CafecontableTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AuthTokenManager.inicializar(applicationContext)

        val sessionManager = SessionManager(applicationContext)

        val compraVm = AppModule.provideCompraCafeViewModel(this)

        val ventaVm = VentaCafeViewModel(
            AppModule.provideVentaUseCase(this)
        )

        val preparacionEntregaVm = AppModule.providePreparacionEntregaViewModel(this)


        val ventasPedidoVm = AppModule.provideVentasPedidoViewModel(this)
        val asignacionMezclaPedidoVm = AppModule.provideAsignacionMezclaPedidoViewModel(this)
        val mezclaVm = AppModule.provideMezclasViewModel(this)
        val liquidacionVm = AppModule.provideLiquidacionViewModel(this)
        val operacionesgastosVm = AppModule.provideOperacionViewModel(this)
        val utilidadVm = AppModule.provideUtilidadViewModel(this)



//        provideMezclasViewModel



//        com/ethandev/cafecontable/ui/screen/historialcompras/HistorialComprasViewModel.kt
//        val (_, _, listarInventarioUseCase) = AppModule.provideInventarioUseCases(this)
//        val inventarioVm = InventarioViewModel(
//            listarInventarioUseCase
//        )
        val inventarioVm = AppModule.provideInventarioViewModel(this)

        val historialComprasVm = HistorialComprasViewModel(
            AppModule.provideHistorialCompraUseCase(this),
            AppModule.provideAnularCompraUseCase(this),
            AppModule.provideObtenerCompraPorIdUseCase(this),
            AppModule.provideActualizarCompraUseCase(this),
            AppModule.provideObtenerMovimientoKardexPorCompraIdUseCase(this),
            AppModule.provideActualizarMovimientoKardexUseCase(this)

        )

//        private val obtenerHistorialComprasUseCase: ObtenerHistorialComprasUseCase,
//        private val anularCompraUseCase: AnularCompraUseCase,
//        private val obtenerCompraPorIdUseCase: ObtenerCompraPorIdUseCase,
//        private val actualizarCompraUseCase: ActualizarCompraUseCase

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


        val prestamosVm = PrestamosViewModel(

            AppModule.provideRegistrarPrestamoUseCase(this),
            AppModule.provideListarPrestamosUseCase(this),
            AppModule.provideBuscarPrestamosUseCase(this),
            AppModule.provideRegistrarAbonoPrestamoUseCase(this),
            AppModule.provideListarAbonoPrestamoUseCase(this)

        )


        val loginVm = AppModule.provideLoginViewModel(this)


        setContent {
            CafecontableTheme {

                var verificandoSesion by remember {
                    mutableStateOf(true)
                }

                var sesionIniciada by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(Unit) {
                    try {
                        val usuario = sessionManager.usuarioSesion.first()

                        val token = AuthTokenManager.obtenerToken()
                            ?: AuthTokenManager.restaurarToken()

                        sesionIniciada =
                            usuario != null &&
                                    !token.isNullOrBlank() &&
                                    AuthTokenManager.tokenVigente(token)

                    } catch (e: Exception) {
                        sesionIniciada = false
                    } finally {
                        verificandoSesion = false
                    }
                }

                when {
                    verificandoSesion -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    !sesionIniciada -> {
                        LoginScreen(
                            viewModel = loginVm,
                            onLoginSuccess = {
                                sesionIniciada = true
                            }
                        )
                    }

                    else -> {
                        AppNavGraph(
                            compraVm = compraVm,
                            ventaVm = ventaVm,
                            ventasPedidoVm = ventasPedidoVm,
                            preparacionEntregaVm = preparacionEntregaVm,
                            inventarioVm = inventarioVm,
                            historialComprasVm = historialComprasVm,
                            historialVentasVm = historialVentasVm,
                            cuentasPorCobrarVm = cuentasPorCobrarVm,
                            cuentasPorPagarVm = cuentasPorPagarVm,
                            prestamosVm = prestamosVm,
                            mezclaVm = mezclaVm,
                            asignacionMezclaPedidoVm = asignacionMezclaPedidoVm,
                            liquidacionVm = liquidacionVm,
                            operacionesgastosVm = operacionesgastosVm,
                            utilidadVm = utilidadVm,
                            onCerrarSesion = {
                                loginVm.cerrarSesion {
                                    AuthTokenManager.limpiarToken()
                                    sesionIniciada = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
