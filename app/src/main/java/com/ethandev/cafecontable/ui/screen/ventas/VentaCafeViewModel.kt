package com.ethandev.cafecontable.ui.screen.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.repository.VentaCafeInput
import com.ethandev.cafecontable.domain.usecase.RegistrarVentaCafeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VentaCafeState(
    val loading: Boolean = false,
    val error: String? = null,
    val okMsg: String? = null
)

class VentaCafeViewModel(
    private val registrarVenta: RegistrarVentaCafeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VentaCafeState())
    val state: StateFlow<VentaCafeState> = _state

    fun limpiarMensajes() {
        _state.value = _state.value.copy(error = null, okMsg = null)
    }

    fun guardar(input: VentaCafeInput) {
        viewModelScope.launch {
            _state.value = VentaCafeState(loading = true)
            runCatching { registrarVenta(input) }
                .onSuccess { _state.value = VentaCafeState(okMsg = "Venta guardada ✅") }
                .onFailure { _state.value = VentaCafeState(error = it.message ?: "Error") }
        }
    }
}