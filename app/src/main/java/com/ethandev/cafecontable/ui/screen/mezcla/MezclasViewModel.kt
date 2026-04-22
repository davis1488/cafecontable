package com.ethandev.cafecontable.ui.screen.mezcla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.entity.CompraDisponibleDb
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.constants.TipoOperacion
import com.ethandev.cafecontable.domain.model.AgregarComprasAMezclaInput
import com.ethandev.cafecontable.domain.model.MezclaDetalleInput
import com.ethandev.cafecontable.domain.model.MezclaHistorialItem
import com.ethandev.cafecontable.domain.model.RegistrarMezclaInput
import com.ethandev.cafecontable.domain.usecase.AgregarComprasAMezclaUseCase
import com.ethandev.cafecontable.domain.usecase.ListarOperacionesPorTipoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaAnalizadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaEntregadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaPendienteEntregaUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialMezclasUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarMezclaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MezclaFormItem(
    val compraId: String,
    val productoId: String,
    val productoNombre: String,
    val cantidadUsada: Double,
    val costoUnitCompra: Long,
    val operacionMezclaId: String? = null
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
    val operacionesMezcla: List<OperacionEntity> = emptyList(),
    val operacionMezclaIdSeleccionada: String? = null,
    val okMsg: String? = null,
    val error: String? = null,
    val loadingHistorial: Boolean = false,
    val historialMezclas: List<MezclaHistorialItem> = emptyList(),
    val mezclaEditandoId: String? = null,
    val editandoMezcla: Boolean = false
)

class MezclasViewModel(
    private val registrarMezclaUseCase: RegistrarMezclaUseCase,
    private val obtenerHistorialMezclasUseCase: ObtenerHistorialMezclasUseCase,
    private val marcarMezclaPendienteEntregaUseCase: MarcarMezclaPendienteEntregaUseCase,
    private val marcarMezclaEntregadoUseCase: MarcarMezclaEntregadoUseCase,
    private val marcarMezclaAnalizadoUseCase: MarcarMezclaAnalizadoUseCase,
    private val listarOperacionesPorTipoUseCase: ListarOperacionesPorTipoUseCase,
    private val compraCafeDao: CompraDao,
    private val agregarComprasAMezclaUseCase: AgregarComprasAMezclaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MezclasState())
    val state: StateFlow<MezclasState> = _state.asStateFlow()

    init {
        cargarOperacionesMezcla()
        cargarComprasDisponibles()
        cargarHistorialMezclas()
    }

    fun limpiarMensajes() {
        _state.update {
            it.copy(
                okMsg = null,
                error = null
            )
        }
    }

    fun seleccionarOperacionMezcla(operacionId: String) {
        _state.update { it.copy(operacionMezclaIdSeleccionada = operacionId) }
    }

    fun iniciarEdicionMezcla(mezclaId: String) {
        _state.update {
            it.copy(
                mezclaEditandoId = mezclaId,
                editandoMezcla = true,
                okMsg = null,
                error = null
            )
        }
    }

    fun cancelarEdicionMezcla() {
        _state.update {
            it.copy(
                mezclaEditandoId = null,
                editandoMezcla = false,
                okMsg = null,
                error = null
            )
        }
    }

    fun cargarOperacionesMezcla() {
        viewModelScope.launch {
            runCatching {
                listarOperacionesPorTipoUseCase(TipoOperacion.MEZCLA)
            }.onSuccess { operaciones ->
                _state.update { actual ->
                    actual.copy(
                        operacionesMezcla = operaciones,
                        operacionMezclaIdSeleccionada = actual.operacionMezclaIdSeleccionada
                            ?: operaciones.firstOrNull()?.id
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(error = e.message ?: "Error al cargar operaciones de mezcla")
                }
            }
        }
    }

    fun cargarHistorialMezclas() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loadingHistorial = true,
                    error = null
                )
            }

            runCatching {
                obtenerHistorialMezclasUseCase()
            }.onSuccess { historial ->
                _state.update {
                    it.copy(
                        loadingHistorial = false,
                        historialMezclas = historial,
                        error = null
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loadingHistorial = false,
                        historialMezclas = emptyList(),
                        error = e.message ?: "Error al cargar historial de mezclas"
                    )
                }
            }
        }
    }

    fun cargarComprasDisponibles() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loadingCompras = true,
                    error = null
                )
            }

            runCatching {
                compraCafeDao.obtenerComprasDisponiblesParaMezcla()
            }.onSuccess { compras: List<CompraDisponibleDb> ->
                _state.update {
                    it.copy(
                        loadingCompras = false,
                        comprasDisponibles = compras.map { compra ->
                            CompraDisponibleUi(
                                compraId = compra.compraId,
                                productoId = compra.productoId,
                                productoNombre = compra.productoNombre,
                                cantidadDisponible = compra.cantidadDisponible,
                                costoUnitCompra = compra.precioUnitCompra
                            )
                        },
                        error = null
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loadingCompras = false,
                        comprasDisponibles = emptyList(),
                        error = e.message ?: "Error al cargar compras disponibles"
                    )
                }
            }
        }
    }

    fun cargarComprasDisponiblesParaAgregarAMezcla(mezclaId: String) {
        iniciarEdicionMezcla(mezclaId)
        cargarComprasDisponibles()
    }

    fun crearItemDesdeCompra(compra: CompraDisponibleUi): MezclaFormItem {
        return MezclaFormItem(
            compraId = compra.compraId,
            productoId = compra.productoId,
            productoNombre = compra.productoNombre,
            cantidadUsada = compra.cantidadDisponible,
            costoUnitCompra = compra.costoUnitCompra,
            operacionMezclaId = _state.value.operacionMezclaIdSeleccionada
        )
    }

    fun registrarMezcla(
        items: List<MezclaFormItem>,
        nota: String?
    ) {
        if (items.isEmpty()) {
            _state.update {
                it.copy(
                    error = "Debes agregar al menos un item a la mezcla",
                    okMsg = null
                )
            }
            return
        }

        val operacionMezclaId = _state.value.operacionMezclaIdSeleccionada
        if (operacionMezclaId.isNullOrBlank()) {
            _state.update {
                it.copy(
                    error = "Debes seleccionar una operación de mezcla",
                    okMsg = null
                )
            }
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
            _state.update {
                it.copy(
                    error = "Hay datos inválidos o cantidades mayores a las disponibles",
                    okMsg = null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    okMsg = null,
                    error = null
                )
            }

            runCatching {
                registrarMezclaUseCase(
                    RegistrarMezclaInput(
                        fecha = System.currentTimeMillis(),
                        nota = nota?.trim()?.ifBlank { null },
                        operacionMezclaId = operacionMezclaId,
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
                _state.update {
                    it.copy(
                        loading = false,
                        okMsg = "Mezcla registrada correctamente",
                        error = null,
                        mezclaEditandoId = null,
                        editandoMezcla = false
                    )
                }
                cargarComprasDisponibles()
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        okMsg = null,
                        error = e.message ?: "Error al registrar mezcla"
                    )
                }
            }
        }
    }

    fun agregarComprasAMezcla(
        mezclaId: String,
        items: List<MezclaFormItem>,
        nota: String? = null
    ) {
        if (items.isEmpty()) {
            _state.update {
                it.copy(
                    error = "Debes agregar al menos un item a la mezcla",
                    okMsg = null
                )
            }
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
            _state.update {
                it.copy(
                    error = "Hay datos inválidos o cantidades mayores a las disponibles",
                    okMsg = null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    okMsg = null,
                    error = null
                )
            }

            runCatching {
                agregarComprasAMezclaUseCase(
                    AgregarComprasAMezclaInput(
                        mezclaId = mezclaId,
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
                _state.update {
                    it.copy(
                        loading = false,
                        mezclaEditandoId = null,
                        editandoMezcla = false,
                        okMsg = "Compras agregadas a la mezcla correctamente",
                        error = null
                    )
                }
                cargarComprasDisponibles()
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        okMsg = null,
                        error = e.message ?: "Error al actualizar mezcla"
                    )
                }
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
            _state.update { it.copy(loading = true, error = null, okMsg = null) }

            runCatching {
                marcarMezclaPendienteEntregaUseCase(
                    mezclaId = mezclaId,
                    numeroSacosEnviados = numeroSacosEnviados,
                    kilajeEnviado = kilajeEnviado
                )
            }.onSuccess {
                _state.update {
                    it.copy(
                        loading = false,
                        okMsg = "Mezcla enviada correctamente"
                    )
                }
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "Error al pasar a pendiente de entrega"
                    )
                }
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
            _state.update { it.copy(loading = true, error = null, okMsg = null) }

            runCatching {
                marcarMezclaEntregadoUseCase(
                    mezclaId = mezclaId,
                    numeroSacosEntregados = numeroSacosEntregados,
                    kilajeEntregado = kilajeEntregado,
                    lugarEntrega = lugarEntrega
                )
            }.onSuccess {
                _state.update {
                    it.copy(
                        loading = false,
                        okMsg = "Entrega registrada correctamente"
                    )
                }
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "Error al pasar a entregado"
                    )
                }
            }
        }
    }

    fun marcarAnalizado(
        mezclaId: String,
        factorRendimiento: Double
    ) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, okMsg = null) }

            runCatching {
                marcarMezclaAnalizadoUseCase(
                    mezclaId = mezclaId,
                    factorRendimiento = factorRendimiento
                )
            }.onSuccess {
                _state.update {
                    it.copy(
                        loading = false,
                        okMsg = "Análisis registrado correctamente"
                    )
                }
                cargarHistorialMezclas()
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "Error al pasar a analizado"
                    )
                }
            }
        }
    }
}