package com.ethandev.cafecontable.ui.screen.ventaspedido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.VentaPedidoInput
import com.ethandev.cafecontable.domain.usecase.ListarVentasPedidoUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarVentaPedidoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VentasPedidoState(
    val loading: Boolean = false,
    val items: List<VentaPedido> = emptyList(),
    val error: String? = null,
    val okMsg: String? = null
)

class VentasPedidoViewModel(
    private val registrarVentaPedidoUseCase: RegistrarVentaPedidoUseCase,
    private val listarVentasPedidoUseCase: ListarVentasPedidoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VentasPedidoState())
    val state: StateFlow<VentasPedidoState> = _state.asStateFlow()

    fun limpiarMensajes() {
        _state.value = _state.value.copy(error = null, okMsg = null)
    }

    fun cargarVentas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            runCatching { listarVentasPedidoUseCase() }
                .onSuccess { ventas ->
                    _state.value = _state.value.copy(
                        loading = false,
                        items = ventas
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Error al cargar ventas"
                    )
                }
        }
    }

    fun registrar(input: VentaPedidoInput) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                okMsg = null
            )

            runCatching {
                registrarVentaPedidoUseCase(input)
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Venta registrada correctamente"
                )
                cargarVentas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al registrar venta"
                )
            }
        }
    }
}