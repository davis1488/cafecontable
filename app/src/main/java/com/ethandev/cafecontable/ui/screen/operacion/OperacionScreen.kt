package com.ethandev.cafecontable.ui.screen.operacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ethandev.cafecontable.domain.constants.TipoOperacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperacionScreen(
    viewModel: OperacionViewModel
) {
    val categoriasGasto = listOf(
        "Transporte",
        "Cargue",
        "Descargue",
        "Empaque",
        "Costales",
        "Mano de obra",
        "Secado",
        "Análisis de café",
        "Comisión",
        "Alimentación",
        "Combustible",
        "Peaje",
        "Otro"
    )


    val operaciones by viewModel.operaciones.collectAsStateWithLifecycle()
    val tipoSeleccionado by viewModel.tipoSeleccionado.collectAsStateWithLifecycle()
    val gastosOperacionSeleccionada by viewModel.gastosOperacionSeleccionada.collectAsStateWithLifecycle()

    val tabs = listOf(
        "Crear operación",
        "Registrar gasto",
        "Gastos por operación"
    )

    val tiposOperacion = listOf(
        TipoOperacion.COMPRA,
        TipoOperacion.MEZCLA,
        TipoOperacion.DESPACHO
    )
    var expandedCategoriaGasto by remember { mutableStateOf(false) }
    var tabIndex by remember { mutableStateOf(0) }
    var mostrarGastosOperacion by remember { mutableStateOf(false) }
    var nombreOperacionDetalle by remember { mutableStateOf("") }

    var nombreOperacion by remember { mutableStateOf("") }
    var descripcionOperacion by remember { mutableStateOf("") }

    var categoriaGasto by remember { mutableStateOf("Transporte") }
    var valorGasto by remember { mutableStateOf("") }
    var descripcionGasto by remember { mutableStateOf("") }
    var terceroGasto by remember { mutableStateOf("") }
    var observacionGasto by remember { mutableStateOf("") }

    var expandedTipoOperacion by remember { mutableStateOf(false) }
    var expandedOperacion by remember { mutableStateOf(false) }

    var operacionSeleccionadaId by remember {
        mutableStateOf<String?>(null)
    }

    val operacionSeleccionada = operaciones.firstOrNull {
        it.id == operacionSeleccionadaId
    }

    LaunchedEffect(Unit) {
        viewModel.cargarTodo()
    }

    LaunchedEffect(operaciones) {
        if (
            operacionSeleccionadaId == null ||
            operaciones.none { it.id == operacionSeleccionadaId }
        ) {
            operacionSeleccionadaId = operaciones.firstOrNull()?.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Gastos y operaciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = { Text(titulo) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (tabIndex) {

            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Crear operación",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expandedTipoOperacion,
                                    onExpandedChange = {
                                        expandedTipoOperacion = !expandedTipoOperacion
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = tipoSeleccionado,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Tipo de operación") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expandedTipoOperacion
                                            )
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedTipoOperacion,
                                        onDismissRequest = {
                                            expandedTipoOperacion = false
                                        }
                                    ) {
                                        tiposOperacion.forEach { tipo ->
                                            DropdownMenuItem(
                                                text = { Text(tipo) },
                                                onClick = {
                                                    viewModel.seleccionarTipo(tipo)
                                                    expandedTipoOperacion = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = nombreOperacion,
                                    onValueChange = { nombreOperacion = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nombre operación") },
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = descripcionOperacion,
                                    onValueChange = { descripcionOperacion = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Descripción") }
                                )

                                Button(
                                    onClick = {
                                        if (nombreOperacion.isNotBlank()) {
                                            viewModel.crearOperacion(
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
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Registrar gasto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                ExposedDropdownMenuBox(
                                    expanded = expandedOperacion,
                                    onExpandedChange = {
                                        expandedOperacion = !expandedOperacion
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = operacionSeleccionada?.let {
                                            "${it.nombre} - ${it.tipo}"
                                        } ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Operación") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expandedOperacion
                                            )
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedOperacion,
                                        onDismissRequest = {
                                            expandedOperacion = false
                                        }
                                    ) {
                                        operaciones.forEach { operacion ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text("${operacion.nombre} - ${operacion.tipo}")
                                                },
                                                onClick = {
                                                    operacionSeleccionadaId = operacion.id
                                                    expandedOperacion = false
                                                }
                                            )
                                        }
                                    }
                                }

                                ExposedDropdownMenuBox(
                                    expanded = expandedCategoriaGasto,
                                    onExpandedChange = {
                                        expandedCategoriaGasto = !expandedCategoriaGasto
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = categoriaGasto,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        label = { Text("Categoría") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expandedCategoriaGasto
                                            )
                                        }
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expandedCategoriaGasto,
                                        onDismissRequest = {
                                            expandedCategoriaGasto = false
                                        }
                                    ) {
                                        categoriasGasto.forEach { categoria ->
                                            DropdownMenuItem(
                                                text = { Text(categoria) },
                                                onClick = {
                                                    categoriaGasto = categoria
                                                    expandedCategoriaGasto = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = valorGasto,
                                    onValueChange = {
                                        valorGasto = it.filter { c -> c.isDigit() }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Valor") },
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = descripcionGasto,
                                    onValueChange = { descripcionGasto = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Descripción gasto") }
                                )

                                OutlinedTextField(
                                    value = terceroGasto,
                                    onValueChange = { terceroGasto = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Tercero") },
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = observacionGasto,
                                    onValueChange = { observacionGasto = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Observación") }
                                )

                                Button(
                                    onClick = {
                                        val operacionId = operacionSeleccionadaId ?: return@Button
                                        val valor = valorGasto.toLongOrNull() ?: 0L

                                        if (categoriaGasto.isNotBlank() && valor > 0L) {
                                            viewModel.registrarGasto(
                                                operacionId = operacionId,
                                                categoria = categoriaGasto.trim(),
                                                descripcion = descripcionGasto.ifBlank { null },
                                                valor = valor,
                                                tercero = terceroGasto.ifBlank { null },
                                                observacion = observacionGasto.ifBlank { null }
                                            )

                                            categoriaGasto = ""
                                            valorGasto = ""
                                            descripcionGasto = ""
                                            terceroGasto = ""
                                            observacionGasto = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = operacionSeleccionada != null
                                ) {
                                    Text("Guardar gasto")
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(operaciones) { operacion ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = operacion.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text("Tipo: ${operacion.tipo}")

                                if (!operacion.descripcion.isNullOrBlank()) {
                                    Text("Descripción: ${operacion.descripcion}")
                                }

                                Text(
                                    text = "Total gastos: $${operacion.totalGastos}",
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        nombreOperacionDetalle = operacion.nombre
                                        viewModel.cargarGastosDeOperacion(operacion.id)
                                        mostrarGastosOperacion = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ver gastos")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (mostrarGastosOperacion) {
        AlertDialog(
            onDismissRequest = {
                mostrarGastosOperacion = false
            },
            title = {
                Text("Gastos de $nombreOperacionDetalle")
            },
            text = {
                if (gastosOperacionSeleccionada.isEmpty()) {
                    Text("Esta operación todavía no tiene gastos registrados.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(gastosOperacionSeleccionada) { gasto ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = gasto.categoria,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text("Valor: $${gasto.valor}")

                                    if (!gasto.descripcion.isNullOrBlank()) {
                                        Text("Descripción: ${gasto.descripcion}")
                                    }

                                    if (!gasto.tercero.isNullOrBlank()) {
                                        Text("Tercero: ${gasto.tercero}")
                                    }

                                    if (!gasto.observacion.isNullOrBlank()) {
                                        Text("Observación: ${gasto.observacion}")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarGastosOperacion = false
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}