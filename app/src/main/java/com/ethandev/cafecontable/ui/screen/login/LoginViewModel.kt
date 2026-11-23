
package com.ethandev.cafecontable.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.local.session.SessionManager
import com.ethandev.cafecontable.data.remote.auth.AuthTokenManager
import com.ethandev.cafecontable.data.remote.auth.LoginResponseDto
import com.ethandev.cafecontable.data.repository.DispositivoRepositoryImpl
import com.ethandev.cafecontable.domain.usecase.CerrarSesionUseCase
import com.ethandev.cafecontable.domain.usecase.IniciarSesionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LoginUiState(
    val cargando: Boolean = false,
    val usuario: LoginResponseDto? = null,
    val error: String? = null
)

class LoginViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val sessionManager: SessionManager,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
    private val dispositivoRepository: DispositivoRepositoryImpl

) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState

    fun prepararRenovacion() {
        if (!_uiState.value.cargando) {
            _uiState.value = LoginUiState()
        }
    }

    fun iniciarSesion(
        correo: String,
        password: String,
        soloRenovacion: Boolean = false
    ) {

        if (_uiState.value.cargando) return

        viewModelScope.launch {
            _uiState.value = LoginUiState(cargando = true)

            try {
                // No permitir cambiar de usuario/empresa conservando datos offline.
                val sesionAnterior = sessionManager.usuarioSesion.first()


                val empresaVinculada =
                    sessionManager.obtenerEmpresaVinculada()



                val loginIncompleto =
                    AuthTokenManager.existeLoginIncompleto()

                if (loginIncompleto && soloRenovacion) {
                    _uiState.value = LoginUiState(
                        error = "La sesión anterior quedó incompleta. " +
                                "Inicia sesión nuevamente con tus credenciales."
                    )
                    return@launch
                }

                if (loginIncompleto) {
                    android.util.Log.w(
                        "CafeContableSeguridad",
                        "P20.12: recuperación de login incompleto " +
                                "mediante autenticación online"
                    )
                }




                if (soloRenovacion && sesionAnterior == null) {
                    _uiState.value = LoginUiState(
                        error = "No hay sesión local para renovar"
                    )
                    return@launch
                }

                if (AuthTokenManager.existeLoginIncompleto() &&
                    sesionAnterior == null
                ) {
                    _uiState.value = LoginUiState(
                        error = "Se detectó un inicio de sesión interrumpido " +
                                "sin identidad local confirmada. " +
                                "Se requiere recuperación segura antes de continuar."
                    )
                    return@launch
                }




                val resultado = iniciarSesionUseCase(
                    correo = correo,
                    password = password
                )

                resultado.fold(
                    onSuccess = { usuario ->
                        // Impedir sustituir la sesión local de otra identidad.
                        if (sesionAnterior != null &&
                            (usuario.usuarioId != sesionAnterior.usuarioId ||
                                    usuario.empresaId != sesionAnterior.empresaId)
                        ) {
                            _uiState.value = LoginUiState(
                                error = "Cierra la sesión actual antes de cambiar de usuario o empresa"
                            )
                            return@fold
                        }

                        val refreshToken = usuario.refreshToken
                            ?.takeIf { it.isNotBlank() }

                        if (refreshToken == null) {
                            _uiState.value = LoginUiState(
                                error = "El servidor no entregó el token de renovación"
                            )
                            return@fold
                        }
                        android.util.Log.e(
                            "CafeContableSeguridad",
                            "LOGIN: Iniciando preparacion del dispositivo"
                        )

                        // P20.12: impedir iniciar sesión en otra empresa
                        // cuando existen datos locales vinculados.
                        if (empresaVinculada != null &&
                            !empresaVinculada.equals(
                                usuario.empresaId,
                                ignoreCase = true
                            )
                        ) {
                            _uiState.value = LoginUiState(
                                error = "Este dispositivo está vinculado a otra empresa. " +
                                        "No se permite cambiar de empresa conservando datos locales."
                            )
                            return@fold
                        }


                        // Primero registrar/vincular con el servidor. No activar acceso
                        // local si falla el desafío o la firma ECDSA.
                        dispositivoRepository.prepararDispositivo(
                            accessToken = usuario.accessToken,
                            usuarioId = usuario.usuarioId,
                            empresaId = usuario.empresaId,
                            refreshToken = refreshToken
                        )

                        android.util.Log.e(
                            "CafeContableSeguridad",
                            "LOGIN: Preparacion del dispositivo completada"
                        )


                        // Credenciales y sesión local solo después de vincular.
                        // Si falla la persistencia, no confirmar login.

                        try {
                            // P20.12: registrar que comienza la persistencia.
                            AuthTokenManager.marcarLoginEnProceso()

                            // Guardar JWT y refresh token juntos.
                            AuthTokenManager.guardarCredencialesAtomicas(
                                nuevoAccessToken = usuario.accessToken,
                                nuevoRefreshToken = refreshToken
                            )

                            // Guardar identidad y autorización offline
                            // de siete días en una transacción DataStore.
                            sessionManager.guardarSesionAutorizada(
                                usuarioId = usuario.usuarioId,
                                empresaId = usuario.empresaId,
                                nombre = usuario.nombre,
                                rol = usuario.rol
                            )

                            // Confirmar únicamente después de guardar ambos.
                            AuthTokenManager.confirmarLoginCompletado()

                        } catch (e: CancellationException) {
                            throw e

                        } catch (e: Exception) {
                            android.util.Log.e(
                                "CafeContableSeguridad",
                                "Error durante la persistencia del login",
                                e
                            )

                            // Conservar la marca si hubo una escritura
                            // incompleta. No ejecutar logout remoto.
                            throw IllegalStateException(
                                "No se pudo completar el inicio de sesión",
                                e
                            )
                        }



                        _uiState.value = LoginUiState(usuario = usuario)


                        // PRUEBA TEMPORAL P20.7 - BLOQUE 15
                        if (com.ethandev.cafecontable.BuildConfig.DEBUG) {
                            android.util.Log.d(
                                "CafeContableSeguridad",
                                "TEST: iniciando prueba de recuperación"
                            )

                          //  probarRenovacionFirmada()
                        }
//                        //////////pru
//
//                        if (com.ethandev.cafecontable.BuildConfig.DEBUG) {
//                            android.util.Log.d(
//                                "CafeContableSeguridad",
//                                "Iniciando prueba de renovacion firmada"
//                            )
//
//                            probarRenovacionFirmada()
//                        }
//
//                        //////////////////pru


                    },
                    onFailure = { error ->
                        _uiState.value = LoginUiState(
                            error = error.message ?: "No se pudo iniciar sesión"
                        )
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    error = e.message ?: "No se pudo vincular el dispositivo"
                )
            }
        }
    }



    fun cerrarSesion(
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.cargando) return

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                cargando = true,
                error = null
            )

            try {
                cerrarSesionUseCase()

                _uiState.value = LoginUiState()

                onSuccess()

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                android.util.Log.e(
                    "CafeContableSeguridad",
                    "Error al cerrar sesión",
                    e
                )

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudo cerrar la sesión de forma segura"
                )
            }
        }
    }


//    fun probarRenovacionFirmada() {
//
//
//   //     if (_uiState.value.cargando) return
//
//        android.util.Log.e(
//            "CafeContableSeguridad",
//            "PRUEBA: entrando a probarRenovacionFirmada"
//        )
//
//        if (_uiState.value.cargando) {
//            android.util.Log.e(
//                "CafeContableSeguridad",
//                "PRUEBA BLOQUEADA: cargando = true"
//            )
//            return
//        }
//        viewModelScope.launch {
//            val dispositivoId =
//                AuthTokenManager.obtenerDispositivoId()
//
//            val refreshToken =
//                AuthTokenManager.obtenerRefreshToken()
//
//            if (dispositivoId.isNullOrBlank() ||
//                refreshToken.isNullOrBlank()) {
//                android.util.Log.e(
//                    "CafeContableSeguridad",
//                    "Faltan credenciales para renovar"
//                )
//                return@launch
//            }
//
//            try {
//                val respuesta = dispositivoRepository.renovarSesion(
//                    dispositivoId = dispositivoId,
//                    refreshToken = refreshToken
//                )
//
//                val nuevoRefresh = respuesta.refreshToken
//                    ?.takeIf { it.isNotBlank() }
//                    ?: error("El servidor no devolvió refresh token")
//
//                // Persistir las nuevas credenciales.
////                AuthTokenManager.guardarRefreshToken(nuevoRefresh)
////                AuthTokenManager.guardarToken(
////                    respuesta.accessToken
////                )
//
//                android.util.Log.d(
//                    "CafeContableSeguridad",
//                    "PASS: renovación firmada HTTP 200 y tokens guardados"
//                )
//
//            } catch (e: Exception) {
//                android.util.Log.e(
//                    "CafeContableSeguridad",
//                    "FALLO: renovación firmada. " +
//                            e.javaClass.simpleName
//                )
//            }
//        }
//    }
//
//


}
