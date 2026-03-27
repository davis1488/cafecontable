package com.ethandev.cafecontable.ui.screen.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.usecase.ObtenerInventarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val obtenerInventarioUseCase: ObtenerInventarioUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InventarioState())
    val state: StateFlow<InventarioState> = _state.asStateFlow()

    fun cargarInventario() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null
            )

            try {
                val items = obtenerInventarioUseCase()
                _state.value = _state.value.copy(
                    loading = false,
                    items = items
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al cargar inventario"
                )
            }
        }
    }
}