package com.ethandev.cafecontable.ui.screen.prestamos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.PrestamoModel
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPrestamoInput
import com.ethandev.cafecontable.domain.model.RegistrarPrestamoInput
import com.ethandev.cafecontable.domain.usecase.BuscarPrestamosUseCase
import com.ethandev.cafecontable.domain.usecase.ListarAbonosPrestamoUseCase
import com.ethandev.cafecontable.domain.usecase.ListarPrestamosUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoPrestamoUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarPrestamoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrestamosViewModel(
    private val registrarPrestamoUseCase: RegistrarPrestamoUseCase,
    private val listarPrestamosUseCase: ListarPrestamosUseCase,
    private val buscarPrestamosUseCase: BuscarPrestamosUseCase,
    private val registrarAbonoPrestamoUseCase: RegistrarAbonoPrestamoUseCase,
    private val listarAbonosPrestamoUseCase: ListarAbonosPrestamoUseCase
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _prestamos = MutableStateFlow<List<PrestamoModel>>(emptyList())
    val prestamos: StateFlow<List<PrestamoModel>> = _prestamos.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _yaConsulto = MutableStateFlow(false)
    val yaConsulto: StateFlow<Boolean> = _yaConsulto.asStateFlow()

    fun onBusquedaChange(value: String) {
        _busqueda.value = value
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun registrarPrestamo(input: RegistrarPrestamoInput) {
        viewModelScope.launch {
            try {
                registrarPrestamoUseCase(input)
                _toastMessage.value = "Préstamo registrado correctamente"
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al registrar préstamo"
            }
        }
    }

    fun registrarAbono(input: RegistrarAbonoPrestamoInput) {
        viewModelScope.launch {
            try {
                registrarAbonoPrestamoUseCase(input)
                _toastMessage.value = "Abono registrado correctamente"
                consultarSegunFiltroActual()
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al registrar abono"
            }
        }
    }

    fun consultarPrestamos() {
        viewModelScope.launch {
            try {
                val query = _busqueda.value.trim()
                _yaConsulto.value = true

                if (query.isBlank()) {
                    _toastMessage.value = "Escriba un nombre o cédula, o use Ver todos"
                    _prestamos.value = emptyList()
                    return@launch
                }

                buscarPrestamosUseCase(query).collect { lista ->
                    _prestamos.value = lista
                }
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al consultar préstamos"
            }
        }
    }

    fun consultarTodosLosPrestamos() {
        viewModelScope.launch {
            try {
                _yaConsulto.value = true
                listarPrestamosUseCase().collect { lista ->
                    _prestamos.value = lista
                }
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al consultar préstamos"
            }
        }
    }

    fun limpiarConsulta() {
        _busqueda.value = ""
        _prestamos.value = emptyList()
        _yaConsulto.value = false
    }

    private fun consultarSegunFiltroActual() {
        val query = _busqueda.value.trim()

        if (!_yaConsulto.value) return

        if (query.isBlank()) {
            consultarTodosLosPrestamos()
        } else {
            consultarPrestamos()
        }
    }

    fun listarAbonos(prestamoId: Int) = listarAbonosPrestamoUseCase(prestamoId)
}