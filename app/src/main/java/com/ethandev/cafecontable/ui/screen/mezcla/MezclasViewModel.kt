package com.ethandev.cafecontable.ui.screen.mezcla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.entity.CompraDisponibleDb
import com.ethandev.cafecontable.domain.model.MezclaDetalleInput
import com.ethandev.cafecontable.domain.model.MezclaHistorialItem
import com.ethandev.cafecontable.domain.model.RegistrarMezclaInput
import com.ethandev.cafecontable.domain.usecase.ActualizarEstadoMezclaUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaAnalizadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaEntregadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaPendienteEntregaUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialMezclasUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarMezclaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MezclaFormItem(
    val compraId: String,
    val productoId: String,
    val productoNombre: String,
    val cantidadUsada: Double,
    val costoUnitCompra: Long
)

data class CompraDisponibleUi(
    val compraId: String,
    val productoId: String,
    val productoNombre: String,
    val cantidadDisponible: Double,
    val costoUnitCompra: Long
)

data class MezclasState(
    val loading: Boolean = false,
    val loadingCompras: Boolean = false,
    val comprasDisponibles: List<CompraDisponibleUi> = emptyList(),
    val okMsg: String? = null,
    val error: String? = null,
    val loadingHistorial: Boolean = false,
    val historialMezclas: List<MezclaHistorialItem> = emptyList()
)

class MezclasViewModel(
    private val registrarMezclaUseCase: RegistrarMezclaUseCase,
    private val obtenerHistorialMezclasUseCase: ObtenerHistorialMezclasUseCase,
    //private val actualizarEstadoMezclaUseCase: ActualizarEstadoMezclaUseCase,
    private val marcarMezclaPendienteEntregaUseCase:MarcarMezclaPendienteEntregaUseCase,
    private  val marcarMezclaEntregadoUseCase: MarcarMezclaEntregadoUseCase,
    private  val marcarMezclaAnalizadoUseCase: MarcarMezclaAnalizadoUseCase,
    private val compraCafeDao: CompraDao
) : ViewModel() {

    private val _state = MutableStateFlow(MezclasState())
    val state: StateFlow<MezclasState> = _state.asStateFlow()

    init {
        cargarComprasDisponibles()
        cargarHistorialMezclas()
    }

    fun limpiarMensajes() {
        _state.value = _state.value.copy(
            okMsg = null,
            error = null
        )
    }

    fun cargarHistorialMezclas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loadingHistorial = true,
                error = null
            )

            runCatching {
                obtenerHistorialMezclasUseCase()
            }.onSuccess { historial ->
                _state.value = _state.value.copy(
                    loadingHistorial = false,
                    historialMezclas = historial,
                    error = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loadingHistorial = false,
                    historialMezclas = emptyList(),
                    error = e.message ?: "Error al cargar historial de mezclas"
                )
            }
        }
    }

    fun cargarComprasDisponibles() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loadingCompras = true,
                error = null
            )

            runCatching {
                compraCafeDao.obtenerComprasDisponiblesParaMezcla()
            }.onSuccess { compras: List<CompraDisponibleDb> ->
                _state.value = _state.value.copy(
                    loadingCompras = false,
                    comprasDisponibles = compras.map {
                        CompraDisponibleUi(
                            compraId = it.compraId,
                            productoId = it.productoId,
                            productoNombre = it.productoNombre,
                            cantidadDisponible = it.cantidadDisponible,
                            costoUnitCompra = it.precioUnitCompra
                        )
                    },
                    error = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loadingCompras = false,
                    comprasDisponibles = emptyList(),
                    error = e.message ?: "Error al cargar compras disponibles"
                )
            }
        }
    }

    fun crearItemDesdeCompra(compra: CompraDisponibleUi): MezclaFormItem {
        return MezclaFormItem(
            compraId = compra.compraId,
            productoId = compra.productoId,
            productoNombre = compra.productoNombre,
            cantidadUsada = 0.0,
            costoUnitCompra = compra.costoUnitCompra
        )
    }

    fun registrarMezcla(
        items: List<MezclaFormItem>,
        nota: String?
    ) {
        if (items.isEmpty()) {
            _state.value = _state.value.copy(
                error = "Debes agregar al menos un item a la mezcla",
                okMsg = null
            )
            return
        }

        val comprasDisponiblesMap = _state.value.comprasDisponibles.associateBy { it.compraId }

        val itemsInvalidos = items.any { item ->
            val compraDisponible = comprasDisponiblesMap[item.compraId]

            item.compraId.isBlank() ||
                    item.productoId.isBlank() ||
                    item.productoNombre.isBlank() ||
                    item.cantidadUsada <= 0.0 ||
                    item.costoUnitCompra < 0L ||
                    compraDisponible == null ||
                    item.cantidadUsada > compraDisponible.cantidadDisponible
        }

        if (itemsInvalidos) {
            _state.value = _state.value.copy(
                error = "Hay datos inválidos o cantidades mayores a las disponibles",
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
                registrarMezclaUseCase(
                    RegistrarMezclaInput(
                        fecha = System.currentTimeMillis(),
                        nota = nota?.trim()?.ifBlank { null },
                        items = items.map {
                            MezclaDetalleInput(
                                compraId = it.compraId,
                                productoId = it.productoId,
                                productoNombre = it.productoNombre,
                                cantidadUsada = it.cantidadUsada,
                                costoUnitCompra = it.costoUnitCompra
                            )
                        }
                    )
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Mezcla registrada correctamente",
                    error = null
                )
                cargarComprasDisponibles()
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = null,
                    error = e.message ?: "Error al registrar mezcla"
                )
            }
        }
    }

    fun calcularCantidadTotal(items: List<MezclaFormItem>): Double {
        return items.sumOf { it.cantidadUsada }
    }

    fun calcularCostoTotal(items: List<MezclaFormItem>): Long {
        return items.sumOf { (it.cantidadUsada * it.costoUnitCompra.toDouble()).toLong() }
    }

    fun calcularCostoPromedioKg(items: List<MezclaFormItem>): Long {
        val cantidadTotal = calcularCantidadTotal(items)
        if (cantidadTotal <= 0.0) return 0L
        return (calcularCostoTotal(items) / cantidadTotal).toLong()
    }

    fun marcarPendienteEntrega(
        mezclaId: String,
        numeroSacosEnviados: Int,
        kilajeEnviado: Double
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, okMsg = null)

            runCatching {
                marcarMezclaPendienteEntregaUseCase(
                    mezclaId = mezclaId,
                    numeroSacosEnviados = numeroSacosEnviados,
                    kilajeEnviado = kilajeEnviado
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Mezcla enviada correctamente"
                )
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al pasar a pendiente de entrega"
                )
            }
        }
    }

    fun marcarEntregado(
        mezclaId: String,
        numeroSacosEntregados: Int,
        kilajeEntregado: Double,
        lugarEntrega: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, okMsg = null)

            runCatching {
                marcarMezclaEntregadoUseCase(
                    mezclaId = mezclaId,
                    numeroSacosEntregados = numeroSacosEntregados,
                    kilajeEntregado = kilajeEntregado,
                    lugarEntrega = lugarEntrega
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Entrega registrada correctamente"
                )
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al pasar a entregado"
                )
            }
        }
    }

    fun marcarAnalizado(
        mezclaId: String,
        factorRendimiento: Double
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, okMsg = null)

            runCatching {
                marcarMezclaAnalizadoUseCase(
                    mezclaId = mezclaId,
                    factorRendimiento = factorRendimiento
                )
            }.onSuccess {
                _state.value = _state.value.copy(
                    loading = false,
                    okMsg = "Análisis registrado correctamente"
                )
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Error al pasar a analizado"
                )
            }
        }
    }
}