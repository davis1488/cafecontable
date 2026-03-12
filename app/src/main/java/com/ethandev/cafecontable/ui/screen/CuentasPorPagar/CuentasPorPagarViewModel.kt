package com.ethandev.cafecontable.ui.screen.cuentasporpagar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.AbonoCuentaPorPagarModel
import com.ethandev.cafecontable.domain.model.CuentaPorPagarModel
import com.ethandev.cafecontable.domain.model.RegistrarAbonoInput
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPagarInput
import com.ethandev.cafecontable.domain.usecase.ListarAbonosCuentaPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarCuentasPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoCuentaPorPagarUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CuentasPorPagarState(
    val items: List<CuentaPorPagarModel> = emptyList(),
    val abonos: List<AbonoCuentaPorPagarModel> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val okMsg: String? = null
)

class CuentasPorPagarViewModel(
    private val listarUseCase: ListarCuentasPorPagarUseCase,
    private val registrarAbonoUseCase: RegistrarAbonoCuentaPorPagarUseCase,
    private val listarAbonosUseCase: ListarAbonosCuentaPorPagarUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CuentasPorPagarState())
    val state: StateFlow<CuentasPorPagarState> = _state

    fun cargar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { listarUseCase() }
                .onSuccess {
                    _state.value = _state.value.copy(items = it, loading = false)
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        loading = false,
                        error = it.message ?: "Error"
                    )
                }
        }
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(error = null, okMsg = null)
    }

    fun registrarAbono(cuentaId: String, valor: Long, nota: String?) {
        viewModelScope.launch {
            runCatching {
                registrarAbonoUseCase(
                    RegistrarAbonoInput(
                        cuentaId = cuentaId,
                        valor = valor,
                        nota = nota
                    )
                )
            }.onSuccess {
                _state.value = _state.value.copy(okMsg = "Abono registrado correctamente")
                cargar()
                cargarAbonos(cuentaId)
            }.onFailure {
                _state.value = _state.value.copy(error = it.message ?: "Error")
            }
        }
    }

    fun cargarAbonos(cuentaId: String) {
        viewModelScope.launch {
            runCatching { listarAbonosUseCase(cuentaId) }
                .onSuccess {
                    _state.value = _state.value.copy(abonos = it)
                }
                .onFailure {
                    _state.value = _state.value.copy(error = it.message ?: "Error")
                }
        }
    }

    fun limpiarAbonos() {
        _state.value = _state.value.copy(abonos = emptyList())
    }
}