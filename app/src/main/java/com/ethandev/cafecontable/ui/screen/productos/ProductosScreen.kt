package com.ethandev.cafecontable.ui.screen.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(vm: ProductosViewModel) {

    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val nombresDisponibles = listOf("Cafe", "Pasilla")

    val unidadesdismponibles = listOf("KG", "LB", "ARROBA")

    var nombre by remember { mutableStateOf(nombresDisponibles[0]) }
    var unidad by remember { mutableStateOf(unidadesdismponibles[0]) }
    var precio by remember { mutableStateOf("") }

    var expandedNombre by remember { mutableStateOf(false) }
    var expandedUnidad by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.limpiarError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text("Productos", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(12.dp))

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // ✅ DROPDOWN PRODUCTO (sin ExposedDropdownMenu)
            ExposedDropdownMenuBox(
                expanded = expandedNombre,
                onExpandedChange = { expandedNombre = !expandedNombre }
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Producto") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNombre)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expandedNombre,
                    onDismissRequest = { expandedNombre = false }
                ) {
                    nombresDisponibles.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                nombre = item
                                expandedNombre = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ✅ DROPDOWN UNIDAD (opcional, pero así queda bonito)
            ExposedDropdownMenuBox(
                expanded = expandedUnidad,
                onExpandedChange = { expandedUnidad = !expandedUnidad }
            ) {
                OutlinedTextField(
                    value = unidad,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Unidad") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidad)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expandedUnidad,
                    onDismissRequest = { expandedUnidad = false }
                ) {
                    unidadesdismponibles.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                unidad = item
                                expandedUnidad = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it.filter(Char::isDigit) },
                label = { Text("Precio (COP)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val p = precio.toLongOrNull() ?: 0L
                    vm.guardar(nombre, unidad, p)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Compra")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.fillMaxSize()) {
                items(state.items) { item ->
                    ListItem(
                        headlineContent = { Text(item.nombre) },
                        supportingContent = { Text("${item.unidad} • $${item.precioVenta}") }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}