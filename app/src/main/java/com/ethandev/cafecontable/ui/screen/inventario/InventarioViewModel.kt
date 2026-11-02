package com.ethandev.cafecontable.ui.screen.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.usecase.ObtenerInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialInventarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val obtenerInventarioUseCase: ObtenerInventarioUseCase,
    private val obtenerHistorialInventarioUseCase: ObtenerHistorialInventarioUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(InventarioState())
    val state: StateFlow<InventarioState> = _state.asStateFlow()

    fun cargarInventario() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val items = obtenerInventarioUseCase()
                val movimientos = obtenerHistorialInventarioUseCase()
                _state.value = _state.value.copy(
                    loading = false, items = items, movimientos = movimientos
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false, error = e.message ?: "Error al cargar inventario"
                )
            }
        }
    }
}
