package com.ethandev.cafecontable.ui.screen.historialcompras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import com.ethandev.cafecontable.domain.usecase.ActualizarCompraUseCase
import com.ethandev.cafecontable.domain.usecase.AnularCompraUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerCompraPorIdUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialComprasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistorialComprasState(
    val items: List<CompraHistorialItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class HistorialComprasViewModel(
    private val obtenerHistorialComprasUseCase: ObtenerHistorialComprasUseCase,
    private val anularCompraUseCase: AnularCompraUseCase,
    private val obtenerCompraPorIdUseCase: ObtenerCompraPorIdUseCase,
    private val actualizarCompraUseCase: ActualizarCompraUseCase
) : ViewModel() {

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _state = MutableStateFlow(HistorialComprasState())
    val state: StateFlow<HistorialComprasState> = _state.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null
            )

            runCatching { obtenerHistorialComprasUseCase() }
                .onSuccess { items ->
                    _state.value = HistorialComprasState(
                        items = items,
                        loading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _state.value = HistorialComprasState(
                        items = emptyList(),
                        loading = false,
                        error = error.message ?: "Error al cargar historial"
                    )
                }
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun editarCompra(item: CompraHistorialItem) {
        _toastMessage.value = "Editando compra de ${item.productoNombre}"
    }

    fun anularCompra(id: Int) {
        viewModelScope.launch {
            try {
                anularCompraUseCase(id)
                _toastMessage.value = "Compra anulada correctamente"
                cargar()
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al anular compra"
            }
        }
    }

    fun actualizarCompra(
        id: Int,
        proveedor: String,
        cantidad: Double,
        precio: Long,
        nota: String
    ) {
        viewModelScope.launch {
            try {
                if (cantidad <= 0.0) {
                    _toastMessage.value = "La cantidad debe ser mayor a 0"
                    return@launch
                }

                if (precio <= 0.0) {
                    _toastMessage.value = "El precio debe ser mayor a 0"
                    return@launch
                }

                val compra = obtenerCompraPorIdUseCase(id)

                if (compra == null) {
                    _toastMessage.value = "No se encontró la compra"
                    return@launch
                }

                val compraActualizada = compra.copy(
                    proveedor = proveedor.ifBlank { null },
                    cantidad = cantidad,
                    precioUnitCompra = precio,
                    nota = nota.ifBlank { null }
                )

                actualizarCompraUseCase(compraActualizada)
                _toastMessage.value = "Compra actualizada correctamente"
                cargar()
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Error al actualizar compra"
            }
        }
    }
}