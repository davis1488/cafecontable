package com.ethandev.cafecontable.ui.screen.compras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.constants.OPERACION_NUEVA_ID
import com.ethandev.cafecontable.domain.constants.TipoOperacion
import com.ethandev.cafecontable.domain.model.CompraCafeImput
import com.ethandev.cafecontable.domain.usecase.ListarOperacionesPorTipoUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarCompraCafeUseCase
import com.ethandev.cafecontable.domain.usecase.ResolverOperacionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompraCafeState(
    val loading: Boolean = false,
    val error: String? = null,
    val okMsg: String? = null,
    val operacionesCompra: List<OperacionEntity> = emptyList(),
    val operacionCompraIdSeleccionada: String? = null
)

class CompraCafeViewModel(
    private val registrarCompra: RegistrarCompraCafeUseCase,
    private val listarOperacionesPorTipoUseCase: ListarOperacionesPorTipoUseCase,
    private val resolverOperacionUseCase: ResolverOperacionUseCase

) : ViewModel() {

    private val _state = MutableStateFlow(CompraCafeState())
    val state: StateFlow<CompraCafeState> = _state

    init {
        cargarOperacionesCompra()
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(error = null, okMsg = null)
    }

    fun limpiarOperacionSeleccionada() {
        _state.update { it.copy(operacionCompraIdSeleccionada = null) }
    }

    fun cargarOperacionesCompra() {
        viewModelScope.launch {
            runCatching {
                listarOperacionesPorTipoUseCase(TipoOperacion.COMPRA)
            }.onSuccess { operaciones ->
                val ultimaOperacionId = operaciones.lastOrNull()?.id ?: OPERACION_NUEVA_ID

                _state.update { actual ->
                    actual.copy(
                        operacionesCompra = operaciones,
                        operacionCompraIdSeleccionada =
                        actual.operacionCompraIdSeleccionada
                            ?.takeIf { seleccionada ->
                                seleccionada == OPERACION_NUEVA_ID || operaciones.any { it.id == seleccionada }
                            }
                            ?: ultimaOperacionId
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(error = e.message ?: "Error al cargar operaciones de compra")
                }
            }
        }
    }

    fun seleccionarOperacionCompra(operacionId: String) {
        _state.update { it.copy(operacionCompraIdSeleccionada = operacionId) }
    }

    fun guardar(input: CompraCafeImput) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                okMsg = null
            )

            runCatching {
                val operacionCompraIdFinal = resolverOperacionUseCase(
                    operacionIdSeleccionada = _state.value.operacionCompraIdSeleccionada,
                    tipo = TipoOperacion.COMPRA,
                    nombreAutomatico = "COMPRA ${System.currentTimeMillis()}",
                    descripcionAutomatica = "Generada automáticamente desde compras"
                )

                val inputFinal = input.copy(
                    operacionCompraId = operacionCompraIdFinal
                )

                registrarCompra(inputFinal)
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Compra guardada ✅"
                )
                cargarOperacionesCompra()
            }.onFailure {
                _state.value = _state.value.copy(
                    loading = false,
                    error = it.message ?: "Error"
                )
            }
        }
    }
}