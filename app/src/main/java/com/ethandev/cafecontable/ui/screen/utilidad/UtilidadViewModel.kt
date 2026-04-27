package com.ethandev.cafecontable.ui.screen.utilidad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.UtilidadOperacionModel
import com.ethandev.cafecontable.domain.model.UtilidadPendienteModel
import com.ethandev.cafecontable.domain.repository.ResumenUtilidadModel
import com.ethandev.cafecontable.domain.usecase.CalcularUtilidadUseCase
import com.ethandev.cafecontable.domain.usecase.ListarHistorialUtilidadesUseCase
import com.ethandev.cafecontable.domain.usecase.ListarUtilidadesPendientesUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerResumenUtilidadUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UtilidadUiState(
    val cargando: Boolean = false,
    val pendientes: List<UtilidadPendienteModel> = emptyList(),
    val historial: List<UtilidadOperacionModel> = emptyList(),
    val resumen: ResumenUtilidadModel? = null,
    val mensaje: String? = null,
    val error: String? = null
)

class UtilidadViewModel(
    private val listarPendientesUseCase: ListarUtilidadesPendientesUseCase,
    private val listarHistorialUseCase: ListarHistorialUtilidadesUseCase,
    private val calcularUtilidadUseCase: CalcularUtilidadUseCase,
    private val obtenerResumenUseCase: ObtenerResumenUtilidadUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UtilidadUiState())
    val state: StateFlow<UtilidadUiState> = _state

    init {
        cargarTodo()
    }

    fun cargarTodo() {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null, mensaje = null) }

            runCatching {
                val pendientes = listarPendientesUseCase()
                val historial = listarHistorialUseCase()
                val resumen = obtenerResumenUseCase()

                _state.update {
                    it.copy(
                        cargando = false,
                        pendientes = pendientes,
                        historial = historial,
                        resumen = resumen
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        cargando = false,
                        error = e.message ?: "Error cargando utilidades"
                    )
                }
            }
        }
    }

    fun calcularUtilidad(
        pendiente: UtilidadPendienteModel,
        nota: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null, mensaje = null) }

            runCatching {
                calcularUtilidadUseCase(pendiente, nota)
            }.onSuccess {
                _state.update {
                    it.copy(
                        cargando = false,
                        mensaje = "Utilidad calculada correctamente"
                    )
                }
                cargarTodo()
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        cargando = false,
                        error = e.message ?: "Error calculando utilidad"
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _state.update { it.copy(mensaje = null, error = null) }
    }
}