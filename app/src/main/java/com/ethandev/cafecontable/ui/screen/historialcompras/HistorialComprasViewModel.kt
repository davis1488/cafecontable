package com.ethandev.cafecontable.ui.screen.historialcompras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialComprasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistorialComprasState(
    val items: List<CompraHistorialItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class HistorialComprasViewModel(
    private val obtenerHistorialCompras: ObtenerHistorialComprasUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistorialComprasState())
    val state: StateFlow<HistorialComprasState> = _state

    fun cargar() {
        viewModelScope.launch {
            _state.value = HistorialComprasState(loading = true)
            runCatching { obtenerHistorialCompras() }
                .onSuccess { _state.value = HistorialComprasState(items = it) }
                .onFailure { _state.value = HistorialComprasState(error = it.message ?: "Error") }
        }
    }
}