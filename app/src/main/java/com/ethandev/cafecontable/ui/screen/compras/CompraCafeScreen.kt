package com.ethandev.cafecontable.ui.screen.compras

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.repository.CompraCafeImput

fun parseCantidad(input: String): Double {
    val texto = input.replace(" ", "").replace(",", ".")
    if (texto.isBlank()) return 0.0

    return try {
        texto
            .split("+")
            .filter { it.isNotBlank() }
            .sumOf { it.toDouble() }
    } catch (e: Exception) {
        0.0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompraCafeScreen(vm: CompraCafeViewModel) {

    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val productos = listOf("Cafe", "Pasilla", "Regular")
    val unidades = listOf("KG")

    var producto by remember { mutableStateOf(productos[0]) }
    var unidad by remember { mutableStateOf(unidades[0]) }
    var cantidadTxt by remember { mutableStateOf("") }
    var precioTxt by remember { mutableStateOf("") }
    var proveedorTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }
    var esCredito by remember { mutableStateOf(false) }
    var abonoTxt by remember { mutableStateOf("") }

    var expandedProducto by remember { mutableStateOf(false) }
    var expandedUnidad by remember { mutableStateOf(false) }

    val cantidad = parseCantidad(cantidadTxt)
    val abono = abonoTxt.toLongOrNull() ?: 0L
    val precio = precioTxt.toLongOrNull() ?: 0L
    val total = if (cantidad > 0 && precio > 0) (cantidad * precio).toLong() else 0L
    val saldo = total - abono

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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Registrar compra de café",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expandedProducto,
                    onExpandedChange = { expandedProducto = !expandedProducto }
                ) {
                    OutlinedTextField(
                        value = producto,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Producto") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expandedProducto)
                        },
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
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expandedUnidad,
                    onExpandedChange = { expandedUnidad = !expandedUnidad }
                ) {
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expandedUnidad)
                        },
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
            }

            item {
                OutlinedTextField(
                    value = cantidadTxt,
                    onValueChange = { cantidadTxt = it.replace(',', '.') },
                    label = { Text("Cantidad ($unidad)") },
                    placeholder = { Text("Ej: 50 o 12.5 o 16+40+55") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = precioTxt,
                    onValueChange = { precioTxt = it.filter(Char::isDigit) },
                    label = { Text("Precio compra unitario (COP)") },
                    placeholder = { Text("Ej: 18000") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = proveedorTxt,
                    onValueChange = { proveedorTxt = it },
                    label = { Text("Proveedor (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = notaTxt,
                    onValueChange = { notaTxt = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("¿Compra a crédito?")
                    Switch(
                        checked = esCredito,
                        onCheckedChange = { esCredito = it }
                    )
                }
            }

            if (esCredito) {
                item {
                    OutlinedTextField(
                        value = abonoTxt,
                        onValueChange = { abonoTxt = it.filter(Char::isDigit) },
                        label = { Text("Abono al crédito (COP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "Saldo pendiente: $saldo COP",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Text(
                    "Total: $total COP",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Button(
                    onClick = {
                        if (cantidad > 0 && precio > 0) {
                            vm.guardar(
                                CompraCafeImput(
                                    productoNombre = producto,
                                    unidad = unidad,
                                    cantidad = cantidad,
                                    precioUnitCompra = precio,
                                    proveedor = proveedorTxt.ifBlank { null },
                                    esCredito = esCredito,
                                    nota = notaTxt.ifBlank { null }
                                )
                            )

                            cantidadTxt = ""
                            precioTxt = ""
                            proveedorTxt = ""
                            notaTxt = ""
                            esCredito = false
                            abonoTxt = ""
                        }
                    },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.loading) "Guardando..." else "Guardar compra")
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}