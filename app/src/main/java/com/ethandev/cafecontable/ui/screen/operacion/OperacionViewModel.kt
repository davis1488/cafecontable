package com.ethandev.cafecontable.ui.screen.operacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.constants.TipoOperacion
import com.ethandev.cafecontable.domain.model.CrearOperacionInput
import com.ethandev.cafecontable.domain.model.GastoOperacionModel
import com.ethandev.cafecontable.domain.model.RegistrarGastoOperacionInput
import com.ethandev.cafecontable.domain.usecase.CrearOperacionUseCase
import com.ethandev.cafecontable.domain.usecase.ListarGastosPorOperacionUseCase
import com.ethandev.cafecontable.domain.usecase.ListarOperacionesUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarGastoOperacionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OperacionViewModel(
    private val crearOperacionUseCase: CrearOperacionUseCase,
    private val registrarGastoUseCase: RegistrarGastoOperacionUseCase,
    private val listarOperacionesUseCase: ListarOperacionesUseCase,
    private val listarGastosPorOperacionUseCase: ListarGastosPorOperacionUseCase
) : ViewModel() {

    private val _operaciones = MutableStateFlow<List<OperacionResumenUi>>(emptyList())
    val operaciones: StateFlow<List<OperacionResumenUi>> = _operaciones.asStateFlow()

    private val _gastosOperacionSeleccionada =
        MutableStateFlow<List<GastoOperacionModel>>(emptyList())

    val gastosOperacionSeleccionada: StateFlow<List<GastoOperacionModel>> =
        _gastosOperacionSeleccionada.asStateFlow()

    private val _tipoSeleccionado = MutableStateFlow(TipoOperacion.COMPRA)
    val tipoSeleccionado: StateFlow<String> = _tipoSeleccionado.asStateFlow()

    init {
        cargarTodo()
    }

    fun seleccionarTipo(tipo: String) {
        _tipoSeleccionado.value = tipo
    }

    fun cargarTodo() {
        viewModelScope.launch {
            val lista = listarOperacionesUseCase()

            _operaciones.value = lista.map { operacion ->
                OperacionResumenUi(
                    id = operacion.id,
                    nombre = operacion.nombre,
                    tipo = operacion.tipo,
                    descripcion = operacion.descripcion,
                    totalGastos = operacion.totalGastos
                )
            }
        }
    }

    fun crearOperacion(
        nombre: String,
        descripcion: String?
    ) {
        viewModelScope.launch {
            crearOperacionUseCase(
                CrearOperacionInput(
                    tipo = _tipoSeleccionado.value,
                    nombre = nombre,
                    descripcion = descripcion
                )
            )

            cargarTodo()
        }
    }

    fun registrarGasto(
        operacionId: String,
        categoria: String,
        descripcion: String?,
        valor: Long,
        tercero: String?,
        observacion: String?
    ) {
        viewModelScope.launch {
            registrarGastoUseCase(
                RegistrarGastoOperacionInput(
                    operacionId = operacionId,
                    categoria = categoria,
                    descripcion = descripcion,
                    valor = valor,
                    tercero = tercero,
                    observacion = observacion
                )
            )

            cargarTodo()
            cargarGastosDeOperacion(operacionId)
        }
    }

    fun cargarGastosDeOperacion(operacionId: String) {
        viewModelScope.launch {
            _gastosOperacionSeleccionada.value =
                listarGastosPorOperacionUseCase(operacionId)
        }
    }
}