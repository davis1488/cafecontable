package com.ethandev.cafecontable.ui.screen.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.repository.VentaCafeInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaCafeScreen(vm: VentaCafeViewModel) {

    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val productos = listOf("Cafe", "Pasilla")
    val unidades = listOf("KG", "LB", "ARROBA")

    var producto by remember { mutableStateOf(productos[0]) }
    var unidad by remember { mutableStateOf(unidades[0]) }
    var cantidadTxt by remember { mutableStateOf("") }
    var precioTxt by remember { mutableStateOf("") }
    var clienteTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }
    var esCredito by remember { mutableStateOf(false) }
    var factorTxt by remember { mutableStateOf("") }
    var cantidadEntregadaTxt by remember { mutableStateOf("") }
    var cantidadPactadaTxt by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }

    var expandedProducto by remember { mutableStateOf(false) }
    var expandedUnidad by remember { mutableStateOf(false) }

    LaunchedEffect(state.error, state.okMsg) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensajes()
        }
        state.okMsg?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensajes()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text("Registrar venta de café", style = MaterialTheme.typography.headlineSmall)

            ExposedDropdownMenuBox(
                expanded = expandedProducto,
                onExpandedChange = { expandedProducto = !expandedProducto }
            ) {
                OutlinedTextField(
                    value = producto,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Producto") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProducto) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expandedProducto,
                    onDismissRequest = { expandedProducto = false }
                ) {
                    productos.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                producto = it
                                expandedProducto = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedUnidad,
                onExpandedChange = { expandedUnidad = !expandedUnidad }
            ) {
                OutlinedTextField(
                    value = unidad,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unidad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedUnidad) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expandedUnidad,
                    onDismissRequest = { expandedUnidad = false }
                ) {
                    unidades.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                unidad = it
                                expandedUnidad = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cantidadTxt,
                onValueChange = { cantidadTxt = it.replace(',', '.') },
                label = { Text("Cantidad (${unidad})") },
                placeholder = { Text("Ej: 10 o 2.5") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precioTxt,
                onValueChange = { precioTxt = it.filter(Char::isDigit) },
                label = { Text("Precio venta unitario (COP)") },
                placeholder = { Text("Ej: 22000") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clienteTxt,
                onValueChange = { clienteTxt = it },
                label = { Text("Cliente (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = factorTxt,
                onValueChange = { factorTxt = it },
                label = { Text("Factor (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notaTxt,
                onValueChange = { notaTxt = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("¿Venta a crédito?")
                Switch(
                    checked = esCredito,
                    onCheckedChange = { esCredito = it }
                )
            }

            val cantidadEntregada = cantidadEntregadaTxt.toDoubleOrNull() ?: 0.0
            val cantidadPactada = cantidadPactadaTxt.toDoubleOrNull() ?: 0.0
            val cantidad = cantidadTxt.toDoubleOrNull() ?: 0.0
            val precio = precioTxt.toLongOrNull() ?: 0L
            val factor = factorTxt.toDoubleOrNull() ?: 0.0
            val total = if (cantidad > 0 && precio > 0) (cantidad * precio).toLong() else 0L

            Text("Total: $total COP", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (cantidad > 0 && precio > 0) {
                        vm.guardar(
                            VentaCafeInput(
                                productoNombre = producto,
                                unidad = unidad,
                                cantidad = cantidad,
                                precioUnitVenta = precio,
                                cliente = clienteTxt.ifBlank { null },
                                esCredito = esCredito,
                                nota = notaTxt.ifBlank { null },
                                factor = factor,
                                cantidadEntregada = cantidadEntregada,
                                cantidadPactada = cantidadPactada,
                                estado = estado,
                                total = total
                            )
                        )

                        cantidadTxt = ""
                        precioTxt = ""
                        clienteTxt = ""
                        notaTxt = ""
                        esCredito = false
                    }
                },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.loading) "Guardando..." else "Guardar venta")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}