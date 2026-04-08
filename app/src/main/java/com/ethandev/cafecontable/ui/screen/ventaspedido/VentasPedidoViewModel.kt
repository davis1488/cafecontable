package com.ethandev.cafecontable.ui.screen.ventaspedido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.VentaPedidoInput
import com.ethandev.cafecontable.domain.usecase.FinalizarVentaPedidoUseCase
import com.ethandev.cafecontable.domain.usecase.ListarVentasPedidoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarVentaPedidoComoAnalizadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarVentaPedidoComoEntregadoUseCase
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
    private val listarVentasPedidoUseCase: ListarVentasPedidoUseCase,
    private val marcarVentaPedidoComoEntregadoUseCase: MarcarVentaPedidoComoEntregadoUseCase,
    private val marcarVentaPedidoComoAnalizadoUseCase: MarcarVentaPedidoComoAnalizadoUseCase,
    private val finalizarVentaPedidoUseCase: FinalizarVentaPedidoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VentasPedidoState())
    val state: StateFlow<VentasPedidoState> = _state.asStateFlow()

    fun limpiarMensajes() {
        _state.value = _state.value.copy(
            error = null,
            okMsg = null
        )
    }

    fun cargarVentas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null
            )

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

    fun marcarEntregado(
        pedidoId: String,
        numeroSacos: Int,
        pesoNeto: Double,
        pesoBruto: Double
    ) {
        if (pedidoId.isBlank()) {
            _state.value = _state.value.copy(
                error = "El pedido es obligatorio",
                okMsg = null
            )
            return
        }

        if (numeroSacos <= 0) {
            _state.value = _state.value.copy(
                error = "El número de sacos debe ser mayor a cero",
                okMsg = null
            )
            return
        }

        if (pesoNeto <= 0.0) {
            _state.value = _state.value.copy(
                error = "El peso neto debe ser mayor a cero",
                okMsg = null
            )
            return
        }

        if (pesoBruto <= 0.0) {
            _state.value = _state.value.copy(
                error = "El peso bruto debe ser mayor a cero",
                okMsg = null
            )
            return
        }

        if (pesoBruto < pesoNeto) {
            _state.value = _state.value.copy(
                error = "El peso bruto no puede ser menor que el peso neto",
                okMsg = null
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                okMsg = null
            )

            runCatching {
                marcarVentaPedidoComoEntregadoUseCase(
                    pedidoId = pedidoId,
                    numeroSacos = numeroSacos,
                    pesoNeto = pesoNeto,
                    pesoBruto = pesoBruto
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Pedido marcado como entregado correctamente"
                )
                cargarVentas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al marcar pedido como entregado"
                )
            }
        }
    }

    fun marcarAnalizado(
        pedidoId: String,
        factor: Double
    ) {
        if (pedidoId.isBlank()) {
            _state.value = _state.value.copy(
                error = "El pedido es obligatorio",
                okMsg = null
            )
            return
        }

        if (factor <= 0.0) {
            _state.value = _state.value.copy(
                error = "El factor debe ser mayor a cero",
                okMsg = null
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                okMsg = null
            )

            runCatching {
                marcarVentaPedidoComoAnalizadoUseCase(
                    pedidoId = pedidoId,
                    factor = factor
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Pedido marcado como analizado correctamente"
                )
                cargarVentas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al marcar pedido como analizado"
                )
            }
        }
    }

    fun finalizarPedido(pedidoId: String) {
        if (pedidoId.isBlank()) {
            _state.value = _state.value.copy(
                error = "El pedido es obligatorio",
                okMsg = null
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                okMsg = null
            )

            runCatching {
                finalizarVentaPedidoUseCase(pedidoId)
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Pedido finalizado correctamente"
                )
                cargarVentas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al finalizar pedido"
                )
            }
        }
    }
}