package com.ethandev.cafecontable.ui.screen.cierre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ethandev.cafecontable.data.repository.CierreSemanalRepository
import com.ethandev.cafecontable.data.repository.CierreSemanalResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CierreSemanalState(
    val cargando: Boolean = false,
    val resumen: CierreSemanalResumen? = null,
    val error: String? = null
)

class CierreSemanalViewModel(private val repo: CierreSemanalRepository) : ViewModel() {
    private val _state = MutableStateFlow(CierreSemanalState())
    val state = _state.asStateFlow()
    private var solicitud = 0

    fun cargar(desde: Long, hastaExclusivo: Long) {
        val token = ++solicitud
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            runCatching { repo.consultar(desde, hastaExclusivo) }
                .onSuccess { if (token == solicitud) _state.value = CierreSemanalState(resumen = it) }
                .onFailure { if (token == solicitud) _state.value = CierreSemanalState(error = it.message ?: "No se pudo generar el reporte") }
        }
    }
}
