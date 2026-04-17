package com.ethandev.cafecontable.ui.screen.operacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.constants.TipoOperacion



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperacionScreen(
    viewModel: OperacionViewModel
) {
    val operaciones by viewModel.operaciones.collectAsStateWithLifecycle()

    var tabIndex by remember { mutableStateOf(0) }

    val tipos = listOf(
        TipoOperacion.COMPRA,
        TipoOperacion.MEZCLA,
        TipoOperacion.DESPACHO
    )

    val tipoSeleccionado = tipos[tabIndex]

    var nombreOperacion by remember { mutableStateOf("") }
    var descripcionOperacion by remember { mutableStateOf("") }

    var categoriaGasto by remember { mutableStateOf("") }
    var valorGasto by remember { mutableStateOf("") }
    var descripcionGasto by remember { mutableStateOf("") }
    var terceroGasto by remember { mutableStateOf("") }
    var observacionGasto by remember { mutableStateOf("") }

    var expandedOperacion by remember { mutableStateOf(false) }
    var operacionSeleccionadaId by remember(tipoSeleccionado) { mutableStateOf<String?>(null) }
    val operacionSeleccionada = operaciones.firstOrNull { it.id == operacionSeleccionadaId }


    LaunchedEffect(tipoSeleccionado) {
        viewModel.cargar(tipoSeleccionado)
    }

    LaunchedEffect(operaciones, tipoSeleccionado) {
        if (operacionSeleccionadaId == null) {
            operacionSeleccionadaId = operaciones.firstOrNull()?.id
        } else {
            val existe = operaciones.any { it.id == operacionSeleccionadaId }
            if (!existe) {
                operacionSeleccionadaId = operaciones.firstOrNull()?.id
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = tabIndex) {
            tipos.forEachIndexed { index, tipo ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(tipo) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Crear operación")
            }

            item {
                OutlinedTextField(
                    value = nombreOperacion,
                    onValueChange = { nombreOperacion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre operación") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = descripcionOperacion,
                    onValueChange = { descripcionOperacion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción") }
                )
            }

            item {
                Button(
                    onClick = {
                        if (nombreOperacion.isNotBlank()) {
                            viewModel.crearOperacion(
                                tipo = tipoSeleccionado,
                                nombre = nombreOperacion.trim(),
                                descripcion = descripcionOperacion.ifBlank { null }
                            )
                            nombreOperacion = ""
                            descripcionOperacion = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar operación")
                }
            }

            item {
                Text("Registrar gasto")
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expandedOperacion,
                    onExpandedChange = { expandedOperacion = !expandedOperacion }
                ) {
                    OutlinedTextField(
                        value = operacionSeleccionada?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar operación") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOperacion)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedOperacion,
                        onDismissRequest = { expandedOperacion = false }
                    ) {
                        operaciones.forEach { operacion ->
                            DropdownMenuItem(
                                text = { Text(operacion.nombre) },
                                onClick = {
                                    operacionSeleccionadaId = operacion.id
                                    expandedOperacion = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = categoriaGasto,
                    onValueChange = { categoriaGasto = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Categoría gasto") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = valorGasto,
                    onValueChange = { valorGasto = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Valor") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = descripcionGasto,
                    onValueChange = { descripcionGasto = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descripción gasto") }
                )
            }

            item {
                OutlinedTextField(
                    value = terceroGasto,
                    onValueChange = { terceroGasto = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tercero") }
                )
            }

            item {
                OutlinedTextField(
                    value = observacionGasto,
                    onValueChange = { observacionGasto = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Observación") }
                )
            }

            item {
                Button(
                    onClick = {
                        val operacionId = operacionSeleccionadaId ?: return@Button
                        viewModel.registrarGasto(
                            tipo = tipoSeleccionado,
                            operacionId = operacionId,
                            categoria = categoriaGasto,
                            descripcion = descripcionGasto.ifBlank { null },
                            valor = valorGasto.toLongOrNull() ?: 0L,
                            tercero = terceroGasto.ifBlank { null },
                            observacion = observacionGasto.ifBlank { null }
                        )
                        categoriaGasto = ""
                        valorGasto = ""
                        descripcionGasto = ""
                        terceroGasto = ""
                        observacionGasto = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar gasto")
                }
            }

            item {
                Text("Operaciones creadas")
            }

            items(operaciones) { operacion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = operacion.nombre)
                        Text(text = "Tipo: ${operacion.tipo}")

                        if (!operacion.descripcion.isNullOrBlank()) {
                            Text(text = "Descripción: ${operacion.descripcion}")
                        }

                        Text(text = "Gastos acumulados: ${operacion.totalGastos}")
                    }
                }
            }
        }
    }
}