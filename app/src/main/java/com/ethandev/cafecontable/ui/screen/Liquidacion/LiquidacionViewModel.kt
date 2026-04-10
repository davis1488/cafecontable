package com.ethandev.cafecontable.ui.screen.liquidacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.LiquidacionHistorial
import com.ethandev.cafecontable.domain.model.LiquidacionPendiente
import com.ethandev.cafecontable.domain.repository.RegistrarLiquidacionInput
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialLiquidacionesUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerLiquidacionesPendientesUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarLiquidacionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiquidacionState(
    val loading: Boolean = false,
    val pendientes: List<LiquidacionPendiente> = emptyList(),
    val historial: List<LiquidacionHistorial> = emptyList(),
    val error: String? = null,
    val okMsg: String? = null
)

class LiquidacionViewModel(
    private val obtenerLiquidacionesPendientesUseCase: ObtenerLiquidacionesPendientesUseCase,
    private val obtenerHistorialLiquidacionesUseCase: ObtenerHistorialLiquidacionesUseCase,
    private val registrarLiquidacionUseCase: RegistrarLiquidacionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LiquidacionState())
    val state: StateFlow<LiquidacionState> = _state.asStateFlow()

    init {
        cargarTodo()
    }

    fun cargarTodo() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, okMsg = null)
            try {
                val pendientes = obtenerLiquidacionesPendientesUseCase()
                val historial = obtenerHistorialLiquidacionesUseCase()

                println("DEBUG pendientes: $pendientes")
                println("DEBUG cantidad: ${pendientes.size}")
                _state.value = _state.value.copy(
                    loading = false,
                    pendientes = pendientes,
                    historial = historial
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error cargando módulo de liquidación"
                )
            }
        }
    }

    fun registrarLiquidacion(
        item: LiquidacionPendiente,
        descuentoCooperativa: Long,
        otrosDescuentos: Long,
        nota: String?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, okMsg = null)
            try {
                registrarLiquidacionUseCase(
                    RegistrarLiquidacionInput(
                        asignacionId = item.asignacionId,
                        pedidoId = item.pedidoId,
                        mezclaId = item.mezclaId,
                        cliente = item.cliente,
                        cantidadKg = item.cantidadKg,
                        precioBaseKg = item.precioBaseKg,
                        factorReal = item.factorReal,
                        descuentoCooperativa = descuentoCooperativa,
                        otrosDescuentos = otrosDescuentos,
                        nota = nota
                    )
                )

                val pendientes = obtenerLiquidacionesPendientesUseCase()
                val historial = obtenerHistorialLiquidacionesUseCase()

                _state.value = _state.value.copy(
                    loading = false,
                    pendientes = pendientes,
                    historial = historial,
                    okMsg = "Liquidación registrada correctamente"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "No se pudo registrar la liquidación"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(error = null, okMsg = null)
    }
}