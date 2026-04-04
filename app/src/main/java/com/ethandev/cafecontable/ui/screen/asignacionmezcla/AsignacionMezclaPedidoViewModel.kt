package com.ethandev.cafecontable.ui.screen.asignacionmezcla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.local.dao.MezclaDao
import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.domain.model.AsignarMezclaAPedidoInput
import com.ethandev.cafecontable.domain.usecase.AsignarMezclaAPedidoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PedidoDisponibleUi(
    val pedidoId: String,
    val clienteNombre: String,
    val productoNombre: String,
    val cantidadPedido: Double,
    val cantidadAsignada: Double,
    val precioUnitVenta: Long


)

data class MezclaDisponibleUi(
    val mezclaId: String,
    val descripcion: String,
    val cantidadDisponible: Double,
    val cantidadTotal: Double,

    val costoPromedioKg: Long
)

data class AsignacionMezclaPedidoState(
    val loading: Boolean = false,
    val loadingDatos: Boolean = false,
    val pedidosDisponibles: List<PedidoDisponibleUi> = emptyList(),
    val mezclasDisponibles: List<MezclaDisponibleUi> = emptyList(),
    val okMsg: String? = null,
    val error: String? = null
)

class AsignacionMezclaPedidoViewModel(
    private val asignarMezclaAPedidoUseCase: AsignarMezclaAPedidoUseCase,
    private val ventaPedidoDao: VentaPedidoDao,
    private val mezclaDao: MezclaDao
) : ViewModel() {

    private val _state = MutableStateFlow(AsignacionMezclaPedidoState())
    val state: StateFlow<AsignacionMezclaPedidoState> = _state.asStateFlow()

    init {
        cargarDatos()
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(
            okMsg = null,
            error = null
        )
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loadingDatos = true,
                error = null
            )

            runCatching {
                val pedidos = ventaPedidoDao.obtenerPedidosDisponiblesParaAsignacion()
                val mezclas = mezclaDao.obtenerMezclasDisponiblesParaAsignacion()
                pedidos to mezclas
            }.onSuccess { (pedidos, mezclas) ->
                _state.value = _state.value.copy(
                    loadingDatos = false,
                    pedidosDisponibles = pedidos.map {
                        PedidoDisponibleUi(
                            pedidoId = it.pedidoId,
                            clienteNombre = it.clienteNombre,
                            productoNombre = it.productoNombre,
                            cantidadPedido = it.cantidadPedido,
                            cantidadAsignada = it.cantidadAsignada,
                            precioUnitVenta = it.precioUnitVenta

                        )
                    },
                    mezclasDisponibles = mezclas.map {
                        MezclaDisponibleUi(
                            mezclaId = it.mezclaId,
                            descripcion = it.descripcion,
                            cantidadDisponible = it.cantidadDisponible,
                            cantidadTotal = it.cantidadTotal,
                            costoPromedioKg = it.costoPromedioKg
                        )
                    },
                    error = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loadingDatos = false,
                    pedidosDisponibles = emptyList(),
                    mezclasDisponibles = emptyList(),
                    error = e.message ?: "Error al cargar pedidos y mezclas"
                )
            }
        }
    }

    fun asignar(
        pedidoId: String,
        mezclaId: String,
        cantidadAsignada: Double,
        precioUnitVenta: Long,
        factor: Double,
        nota: String?
    ) {
        if (pedidoId.isBlank()) {
            _state.value = _state.value.copy(
                error = "Debes seleccionar un anuncio",
                okMsg = null
            )
            return
        }

        if (mezclaId.isBlank()) {
            _state.value = _state.value.copy(
                error = "Debes seleccionar una mezcla",
                okMsg = null
            )
            return
        }

        if (cantidadAsignada <= 0.0) {
            _state.value = _state.value.copy(
                error = "La cantidad asignada debe ser mayor a cero",
                okMsg = null
            )
            return
        }

        if (precioUnitVenta <= 0L) {
            _state.value = _state.value.copy(
                error = "El precio de venta debe ser mayor a cero",
                okMsg = null
            )
            return
        }

        val mezcla = _state.value.mezclasDisponibles.firstOrNull { it.mezclaId == mezclaId }
        if (mezcla == null) {
            _state.value = _state.value.copy(
                error = "La mezcla seleccionada no existe",
                okMsg = null
            )
            return
        }

        if (cantidadAsignada > mezcla.cantidadDisponible) {
            _state.value = _state.value.copy(
                error = "La cantidad asignada supera la disponible de la mezcla",
                okMsg = null
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                okMsg = null,
                error = null
            )

            runCatching {
                asignarMezclaAPedidoUseCase(
                    AsignarMezclaAPedidoInput(
                        pedidoId = pedidoId,
                        mezclaId = mezclaId,
                        fecha = System.currentTimeMillis(),
                        cantidadAsignada = cantidadAsignada,
                        precioUnitVenta = precioUnitVenta,
                        factor = factor,
                        nota = nota?.trim()?.ifBlank { null }
                    )
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Mezcla asignada correctamente al anuncio",
                    error = null
                )
                cargarDatos()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = null,
                    error = e.message ?: "Error al asignar mezcla"
                )
            }
        }
    }

    fun calcularSubtotal(cantidadAsignada: Double, precioUnitVenta: Long): Long {
        return (cantidadAsignada * precioUnitVenta).toLong()
    }
}