package com.ethandev.cafecontable.ui.screen.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.InventarioItemModel
import com.ethandev.cafecontable.domain.usecase.ListarInventarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InventarioScreenState(
    val items: List<InventarioItemModel> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class InventarioViewModel(
    private val listarInventario: ListarInventarioUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InventarioScreenState())
    val state: StateFlow<InventarioScreenState> = _state

    fun cargar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { listarInventario() }
                .onSuccess { _state.value = InventarioScreenState(items = it) }
                .onFailure { _state.value = InventarioScreenState(error = it.message ?: "Error") }
        }
    }
}