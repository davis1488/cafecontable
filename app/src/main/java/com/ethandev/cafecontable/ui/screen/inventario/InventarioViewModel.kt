package com.ethandev.cafecontable.ui.screen.inventario

import com.ethandev.cafecontable.data.local.dao.MezclaDao
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.domain.usecase.ObtenerInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialInventarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val obtenerInventarioUseCase: ObtenerInventarioUseCase,
    private val obtenerHistorialInventarioUseCase: ObtenerHistorialInventarioUseCase,
    private val mezclaDao: MezclaDao
) : ViewModel() {
    private val _state = MutableStateFlow(InventarioState())
    val state: StateFlow<InventarioState> = _state.asStateFlow()

    private val _mezclas = MutableStateFlow<List<SaldoMezclaUi>>(emptyList())
    val mezclas: StateFlow<List<SaldoMezclaUi>> = _mezclas.asStateFlow()

    fun cargarInventario() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val items = obtenerInventarioUseCase()
                val movimientos = obtenerHistorialInventarioUseCase()
                val saldos = mezclaDao.saldosParaInventario()
                val mezclasCalculadas = saldos.map { m ->
                    val total = m.cantidadTotal.coerceAtLeast(0.0)
                    val asignado = m.cantidadAsignada.coerceAtLeast(0.0)
                    val propio = (total - asignado).coerceAtLeast(0.0)
                    val entregado = (m.kilajeEntregado ?: 0.0).coerceIn(0.0, total)
                    val enDeposito = if (m.estado == "ENTREGADO" || m.estado == "ANALIZADO") {
                        (entregado - asignado).coerceAtLeast(0.0).coerceAtMost(propio)
                    } else 0.0
                    val enBodega = if (m.estado == "CREADO") propio else 0.0
                    val enTransito = if (m.estado == "PENDIENTE_ENTREGA") propio else 0.0
                    val sinConciliar = (propio - enDeposito - enBodega - enTransito).coerceAtLeast(0.0)
                    SaldoMezclaUi(m.mezclaId, m.nota, m.estado, total, asignado,
                        enBodega, enTransito, enDeposito, sinConciliar)
                }
                _mezclas.value = mezclasCalculadas
                _state.value = _state.value.copy(
                    loading = false, items = items, movimientos = movimientos
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false, error = e.message ?: "Error al cargar inventario"
                )
            }
        }
    }
}

/** Balance físico de la mezcla (no son movimientos adicionales de Kardex). */
data class SaldoMezclaUi(
    val id: String,
    val nota: String?,
    val estado: String,
    val total: Double,
    val asignado: Double,
    val bodega: Double,
    val transito: Double,
    val deposito: Double,
    val sinConciliar: Double
) {
    val propio: Double get() = bodega + transito + deposito + sinConciliar
}
