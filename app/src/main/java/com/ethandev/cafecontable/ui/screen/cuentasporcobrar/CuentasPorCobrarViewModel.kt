package com.ethandev.cafecontable.ui.screen.cuentasporcobrar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.CuentaPorCobrarModel
import com.ethandev.cafecontable.domain.usecase.ListarCuentasPorCobrarUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CuentasPorCobrarState(
    val items: List<CuentaPorCobrarModel> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class CuentasPorCobrarViewModel(
    private val listarUseCase: ListarCuentasPorCobrarUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CuentasPorCobrarState())
    val state: StateFlow<CuentasPorCobrarState> = _state

    fun cargar() {
        viewModelScope.launch {
            _state.value = CuentasPorCobrarState(loading = true)
            runCatching { listarUseCase() }
                .onSuccess { _state.value = CuentasPorCobrarState(items = it) }
                .onFailure { _state.value = CuentasPorCobrarState(error = it.message ?: "Error") }
        }
    }
}