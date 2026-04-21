package com.ethandev.cafecontable.ui.screen.operacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.domain.model.CrearOperacionInput
import com.ethandev.cafecontable.domain.model.RegistrarGastoOperacionInput
import com.ethandev.cafecontable.domain.usecase.CrearOperacionUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarGastoOperacionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OperacionViewModel(
    private val crearOperacionUseCase: CrearOperacionUseCase,
    private val registrarGastoUseCase: RegistrarGastoOperacionUseCase,
    private val db: AppDatabase
) : ViewModel() {

    private val _operaciones = MutableStateFlow<List<OperacionResumenUi>>(emptyList())
    val operaciones: StateFlow<List<OperacionResumenUi>> = _operaciones.asStateFlow()

    fun cargar(tipo: String) {
        viewModelScope.launch {
            val lista = db.operacionDao().listarPorTipo(tipo)

            val resumen = lista.map { operacion ->
                OperacionResumenUi(
                    id = operacion.id,
                    nombre = operacion.nombre,
                    tipo = operacion.tipo,
                    descripcion = operacion.descripcion,
                    totalGastos = db.gastoOperacionDao().obtenerTotalGastosPorOperacion(operacion.id)
                )
            }

            _operaciones.value = resumen
        }
    }

    fun crearOperacion(tipo: String, nombre: String, descripcion: String?) {
        viewModelScope.launch {
            crearOperacionUseCase(
                CrearOperacionInput(
                    tipo = tipo,
                    nombre = nombre,
                    descripcion = descripcion
                )
            )
            cargar(tipo)
        }
    }

    fun registrarGasto(
        tipo: String,
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
            cargar(tipo)
        }
    }
}