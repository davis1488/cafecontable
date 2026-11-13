
package com.ethandev.cafecontable.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.local.session.SessionManager
import com.ethandev.cafecontable.data.remote.AuthTokenManager
import com.ethandev.cafecontable.data.remote.LoginResponseDto
import com.ethandev.cafecontable.domain.usecase.CerrarSesionUseCase
import com.ethandev.cafecontable.domain.usecase.IniciarSesionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val cargando: Boolean = false,
    val usuario: LoginResponseDto? = null,
    val error: String? = null
)

class LoginViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val sessionManager: SessionManager,
    private val cerrarSesionUseCase: CerrarSesionUseCase

) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState

    fun iniciarSesion(correo: String, password: String) {

        if (_uiState.value.cargando) return

        viewModelScope.launch {

            _uiState.value = LoginUiState(cargando = true)

            val resultado = iniciarSesionUseCase(
                correo = correo,
                password = password
            )

            resultado.fold(
                onSuccess = { usuario ->

                    try {
                        sessionManager.guardarUsuario(
                            usuarioId = usuario.usuarioId,
                            empresaId = usuario.empresaId,
                            nombre = usuario.nombre,
                            rol = usuario.rol
                        )

                        sessionManager.habilitarAccesoOffline()

                        AuthTokenManager.guardarToken(usuario.accessToken)

                        _uiState.value = LoginUiState(
                            usuario = usuario
                        )

                    } catch (e: Exception) {
                        _uiState.value = LoginUiState(
                            error = "No se pudo guardar la sesión local"
                        )
                    }
                },

                onFailure = { error ->

                    _uiState.value = LoginUiState(
                        error = error.message
                            ?: "No se pudo iniciar sesión"
                    )
                }
            )
        }
    }


    fun cerrarSesion(
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                cerrarSesionUseCase()

                AuthTokenManager.limpiarToken()

                sessionManager.cerrarSesion()

                _uiState.value = LoginUiState()

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    error = "No se pudo cerrar la sesión"
                )
            }
        }
    }

}
