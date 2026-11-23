
package com.ethandev.cafecontable

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.ethandev.cafecontable.data.remote.auth.AuthTokenManager
import com.ethandev.cafecontable.data.remote.core.RetrofitClient
import com.ethandev.cafecontable.ui.navigation.AppNavGraph
import com.ethandev.cafecontable.ui.screen.cuentasporcobrar.CuentasPorCobrarViewModel
import com.ethandev.cafecontable.ui.screen.cuentasporpagar.CuentasPorPagarViewModel
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel
import com.ethandev.cafecontable.ui.screen.historialventas.HistorialVentasViewModel
import com.ethandev.cafecontable.ui.screen.login.LoginScreen
import com.ethandev.cafecontable.ui.screen.prestamos.PrestamosViewModel
import com.ethandev.cafecontable.ui.screen.ventas.VentaCafeViewModel
import com.ethandev.cafecontable.ui.theme.CafecontableTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first
import com.ethandev.cafecontable.data.worker.ProgramadorSyncCompras
import com.ethandev.cafecontable.data.worker.ProgramadorLogoutPendiente
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.BuildConfig


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AuthTokenManager.inicializar(applicationContext)



        // P20.11: recuperar revocaciones pendientes
        // incluso cuando el usuario esté en Login.
        try {
            val pendientes =
                AuthTokenManager.obtenerLogoutsPendientes()

            if (pendientes.isNotEmpty()) {
                ProgramadorLogoutPendiente.programar(
                    applicationContext
                )

                Log.d(
                    "LOGOUT_PENDIENTE",
                    "Recuperación programada. " +
                            "Solicitudes pendientes: ${pendientes.size}"
                )
            }
        } catch (e: Exception) {
            Log.e(
                "LOGOUT_PENDIENTE",
                "No se pudo recuperar la cola de revocaciones",
                e
            )
        }


        /////////////////////////prueba

        if (BuildConfig.DEBUG) {
            val dispositivoId =
                AuthTokenManager.obtenerDispositivoId()

            android.util.Log.d(
                "CafeContableSeguridad",
                "Dispositivo local: ${dispositivoId ?: "NO REGISTRADO"}"
            )
        }

        /////////////////////////prueba




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
//prueba
                var ejecutarPruebaVencimiento by remember {
                    mutableStateOf(false)
                }
//
                var renovandoSesion by remember { mutableStateOf(false) }


                LaunchedEffect(Unit) {
                    try {
                        val usuario = sessionManager.usuarioSesion.first()

                        val token = AuthTokenManager.obtenerToken()
                            ?: AuthTokenManager.restaurarToken()

                        // P19: la autorización offline debe verificarse SIEMPRE.
                        val sesionOfflineValida =
                            sessionManager.obtenerSesionOfflineValida() != null

                        // JWT local vigente no equivale a autenticación online.

                        val jwtVigenteLocal =
                            usuario != null &&
                                    !token.isNullOrBlank() &&
                                    AuthTokenManager.tokenCorrespondeASesion(
                                        token = token,
                                        usuarioId = usuario.usuarioId,
                                        empresaId = usuario.empresaId
                                    )


                        // Si el plazo offline expiró, exigir una comprobación real
                        // contra una ruta de la API protegida por JWT.
                        val sesionOnlineConfirmada =
                            if (!sesionOfflineValida && jwtVigenteLocal) {
                                try {
                                    RetrofitClient.api.obtenerCompras().isSuccessful
                                } catch (e: Exception) {
                                    Log.w("P19", "No fue posible confirmar la sesión online", e)
                                    false
                                }
                            } else {
                                false
                            }

                        Log.d(
                            "P19",
                            "JWT local vigente: $jwtVigenteLocal | " +
                                    "Offline vigente: $sesionOfflineValida | " +
                                    "Online confirmado: $sesionOnlineConfirmada"
                        )

                        sesionIniciada =
                            usuario != null &&
                                    (sesionOfflineValida || sesionOnlineConfirmada)

                    } catch (e: Exception) {
                        sesionIniciada = false
                    } finally {
                        verificandoSesion = false
                    }
                }


                LaunchedEffect(verificandoSesion, sesionIniciada) {
                    if (!verificandoSesion) {
                        if (sesionIniciada) {
                            ProgramadorSyncCompras.iniciar(applicationContext)
                        } else {
                            ProgramadorSyncCompras.detener(applicationContext)
                        }
                    }
                }


                // P19: ejecuta el vencimiento de prueba al pulsar el botón.
                // No cierra sesión ni modifica las compras de Room.
                LaunchedEffect(ejecutarPruebaVencimiento) {
                    if (ejecutarPruebaVencimiento) {
                        try {
                            Log.d("P19", "Ejecutando vencimiento de autorización offline")
                            sessionManager.simularVencimientoOfflineParaPrueba()

                            val sesionValida =
                                sessionManager.obtenerSesionOfflineValida()
                            Log.d("P19", "Sesión offline válida: ${sesionValida != null}")

                            val mensaje = if (sesionValida == null) {
                                "P19: autorización offline vencida"
                            } else {
                                "P19: la autorización sigue vigente"
                            }
                            Toast.makeText(applicationContext, mensaje, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e("P19", "Error durante la prueba de vencimiento", e)
                            Toast.makeText(
                                applicationContext,
                                "P19: error al ejecutar la prueba; revisa Logcat",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            ejecutarPruebaVencimiento = false
                        }
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

                    renovandoSesion -> {
                        LoginScreen(
                            viewModel = loginVm,
                            modoRenovacion = true,
                            onCancelar = { renovandoSesion = false },
                            onLoginSuccess = {
                                renovandoSesion = false
                                ProgramadorSyncCompras.sincronizarAhora(applicationContext)
                            }
                        )
                    }


                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            // Botón temporal de P19, separado de la barra de estado.
                            if (
                                (applicationInfo.flags and
                                        android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                            ) {
                                val interaccionP19 = remember { MutableInteractionSource() }
                                val presionadoP19 by interaccionP19.collectIsPressedAsState()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(top = 12.dp, bottom = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = {
                                            Log.d(
                                                "P19",
                                                "Botón presionado: iniciando prueba de vencimiento"
                                            )
                                            ejecutarPruebaVencimiento = true
                                        },
                                        interactionSource = interaccionP19,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (presionadoP19) {
                                                Color(0xFF3E2723) // Marrón oscuro al presionar
                                            } else {
                                                Color(0xFF795548) // Marrón normal
                                            },
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("P19: Vencer autorización offline")
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
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
                                    onRenovarSesion = {
                                        loginVm.prepararRenovacion()
                                        renovandoSesion = true
                                    },
                                    onCerrarSesion = {
                                        loginVm.cerrarSesion {
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
    }
}
