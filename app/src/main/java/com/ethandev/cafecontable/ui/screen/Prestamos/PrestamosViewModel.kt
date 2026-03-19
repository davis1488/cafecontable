package com.ethandev.cafecontable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPrestamoInput
import com.ethandev.cafecontable.domain.model.RegistrarPrestamoInput
import com.ethandev.cafecontable.domain.usecase.BuscarPrestamosUseCase
import com.ethandev.cafecontable.domain.usecase.ListarAbonosPrestamoUseCase
import com.ethandev.cafecontable.domain.usecase.ListarPrestamosUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoPrestamoUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarPrestamoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PrestamosViewModel(
    private val registrarPrestamoUseCase: RegistrarPrestamoUseCase,
    private val listarPrestamosUseCase: ListarPrestamosUseCase,
    private val buscarPrestamosUseCase: BuscarPrestamosUseCase,
    private val registrarAbonoPrestamoUseCase: RegistrarAbonoPrestamoUseCase,
    private val listarAbonosPrestamoUseCase: ListarAbonosPrestamoUseCase
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    val prestamos = _busqueda
        .flatMapLatest { query ->
            if (query.isBlank()) {
                listarPrestamosUseCase()
            } else {
                buscarPrestamosUseCase(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al registrar abono"
            }
        }
    }

    fun listarAbonos(prestamoId: Int) = listarAbonosPrestamoUseCase(prestamoId)
}