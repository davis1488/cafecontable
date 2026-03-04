package com.ethandev.cafecontable.ui.screen.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.Producto
import com.ethandev.cafecontable.domain.usecase.CrearProductoUseCase
import com.ethandev.cafecontable.domain.usecase.ListarProductosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductosState(
    val items: List<Producto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class ProductosViewModel(
    private val crearProducto: CrearProductoUseCase,
    private val listarProductos: ListarProductosUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductosState())
    val state: StateFlow<ProductosState> = _state

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { listarProductos() }
                .onSuccess { _state.value = ProductosState(items = it) }
                .onFailure { _state.value = ProductosState(error = it.message ?: "Error") }
        }
    }

    fun guardar(nombre: String, unidad: String, precio: Long) {
        if(nombre.isBlank() || unidad.isBlank() || precio <= 0L){
            _state.value = _state.value.copy(error = "completa los campos y usa un precio valido")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { crearProducto(nombre, unidad, precio) }
                .onSuccess { cargar() }
                .onFailure { _state.value = _state.value.copy(error = it.message ?: "Error al guardar producto") }
        }
    }

    fun limpiarError () {
        _state.value = _state.value.copy(error = null)
    }
}