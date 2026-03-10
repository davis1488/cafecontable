package com.ethandev.cafecontable.ui.screen.historialventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.VentaHistorialItem
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialVentasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistorialVentasState(
    val items: List<VentaHistorialItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class HistorialVentasViewModel(
    private val obtenerHistorialVentas: ObtenerHistorialVentasUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistorialVentasState())
    val state: StateFlow<HistorialVentasState> = _state

    fun cargar() {
        viewModelScope.launch {
            _state.value = HistorialVentasState(loading = true)
            runCatching { obtenerHistorialVentas() }
                .onSuccess { _state.value = HistorialVentasState(items = it) }
                .onFailure { _state.value = HistorialVentasState(error = it.message ?: "Error") }
        }
    }
}