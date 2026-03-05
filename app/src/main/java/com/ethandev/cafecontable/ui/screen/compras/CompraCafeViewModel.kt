package com.ethandev.cafecontable.ui.screen.compras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.repository.CompraCafeImput
import com.ethandev.cafecontable.domain.usecase.RegistrarCompraCafeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class CompraCafeState(
    val loading: Boolean = false,
    val error: String? = null,
    val okMsg: String? = null
)

class CompraCafeViewModel(
    private val registrarCompra: RegistrarCompraCafeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CompraCafeState())
    val state: StateFlow<CompraCafeState> = _state

    fun limpiarMensajes() {
        _state.value = _state.value.copy(error = null, okMsg = null)
    }

    fun guardar(input: CompraCafeImput) {
        viewModelScope.launch {
            _state.value = CompraCafeState(loading = true)
            runCatching { registrarCompra(input) }
                .onSuccess { _state.value = CompraCafeState(okMsg = "Compra guardada ✅") }
                .onFailure { _state.value = CompraCafeState(error = it.message ?: "Error") }
        }
    }
}