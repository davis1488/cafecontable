package com.ethandev.cafecontable.ui.screen.preparacionentrega

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.model.PreparacionItemInput
import com.ethandev.cafecontable.domain.model.RegistrarPreparacionEntregaInput
import com.ethandev.cafecontable.domain.usecase.RegistrarPreparacionEntregaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PreparacionEntregaState(
    val loading: Boolean = false,
    val error: String? = null,
    val okMsg: String? = null
)

class PreparacionEntregaViewModel(
    private val registrarPreparacionEntregaUseCase: RegistrarPreparacionEntregaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PreparacionEntregaState())
    val state: StateFlow<PreparacionEntregaState> = _state.asStateFlow()

    fun limpiarMensajes() {
        _state.value = _state.value.copy(
            error = null,
            okMsg = null
        )
    }

    fun registrar(
        ventaId: String,
        cantidadCafe: Double,
        cantidadPasilla: Double,
        cantidadRegular: Double,
        precioVentaUnitario: Long,
        factor: Double,
        nota: String?
    ) {
        val ventaIdLimpio = ventaId.trim()
        val notaLimpia = nota?.trim()?.takeIf { it.isNotEmpty() }

        when {
            ventaIdLimpio.isBlank() -> {
                _state.value = _state.value.copy(
                    error = "La venta es obligatoria",
                    okMsg = null
                )
                return
            }

            cantidadCafe < 0 || cantidadPasilla < 0 || cantidadRegular < 0 -> {
                _state.value = _state.value.copy(
                    error = "Las cantidades no pueden ser negativas",
                    okMsg = null
                )
                return
            }

            (cantidadCafe + cantidadPasilla + cantidadRegular) <= 0.0 -> {
                _state.value = _state.value.copy(
                    error = "Debes ingresar al menos una cantidad mayor que cero",
                    okMsg = null
                )
                return
            }

            precioVentaUnitario < 0 -> {
                _state.value = _state.value.copy(
                    error = "El precio de venta unitario no puede ser negativo",
                    okMsg = null
                )
                return
            }
        }

        val items = mutableListOf<PreparacionItemInput>()

        if (cantidadCafe > 0) {
            items.add(
                PreparacionItemInput(
                    productoId = "CAFE",
                    productoNombre = "Café",
                    cantidadUsada = cantidadCafe,
                    costoUnit = 0
                )
            )
        }

        if (cantidadPasilla > 0) {
            items.add(
                PreparacionItemInput(
                    productoId = "PASILLA",
                    productoNombre = "Pasilla",
                    cantidadUsada = cantidadPasilla,
                    costoUnit = 0
                )
            )
        }

        if (cantidadRegular > 0) {
            items.add(
                PreparacionItemInput(
                    productoId = "REGULAR",
                    productoNombre = "Regular",
                    cantidadUsada = cantidadRegular,
                    costoUnit = 0
                )
            )
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                okMsg = null
            )

            runCatching {
                registrarPreparacionEntregaUseCase(
                    RegistrarPreparacionEntregaInput(
                        ventaId = ventaIdLimpio,
                        fecha = System.currentTimeMillis(),
                        precioVentaUnitario = precioVentaUnitario,
                        factor = factor,
                        nota = notaLimpia,
                        items = items
                    )
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    error = null,
                    okMsg = "Preparación registrada correctamente"
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al registrar preparación",
                    okMsg = null
                )
            }
        }
    }
}