package com.ethandev.cafecontable.ui.screen.compras

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.data.local.entity.OperacionEntity
import com.ethandev.cafecontable.domain.repository.CompraCafeImput
import com.ethandev.cafecontable.ui.utils.formatNumber

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

    val context = LocalContext.current
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


    var expandedOperacion by remember { mutableStateOf(false) }

    val operacionesCompra = state.operacionesCompra
    val operacionCompraId = state.operacionCompraIdSeleccionada

    val operacionCompraSeleccionada = operacionesCompra.firstOrNull { it.id == operacionCompraId }


    //val operacionCompraId = state.operacionCompraIdSeleccionada
    //var operacionCompraId by remember { mutableStateOf<String?>(null) }
   // val operacionCompraSeleccionada = operacionesCompra.firstOrNull { it.id == operacionCompraId }
    //var operacionCompraSeleccionada by remember { mutableStateOf<OperacionEntity?>(null) }

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
                    value = formatNumber(cantidadTxt),
                    onValueChange = { input ->
                        cantidadTxt = input.filter(Char::isDigit)
                    },
                    label = { Text("Cantidad ($unidad)") },
                    placeholder = { Text("Ej: 100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = formatNumber(precioTxt),
                    onValueChange = { input ->
                        precioTxt = input.filter(Char::isDigit)
                    },
                    label = { Text("Precio compra unitario (COP)") },
                    placeholder = { Text("Ej: 18000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()

                )
            }

            item {
            ExposedDropdownMenuBox (
                expanded = expandedOperacion,
                onExpandedChange = { expandedOperacion = !expandedOperacion }
            ) {
                OutlinedTextField(
                    value = operacionCompraSeleccionada?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Operación de compra") },
                    isError = operacionCompraSeleccionada == null,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expandedOperacion)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expandedOperacion,
                    onDismissRequest = { expandedOperacion = false }
                ) {
                    operacionesCompra.forEach { operacion ->
                        DropdownMenuItem(
                            text = { Text(operacion.nombre) },
                            onClick = {
                                vm.seleccionarOperacionCompra(operacion.id)
                                expandedOperacion = false
                            }
                        )
                    }
                }
            }
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
                        value = formatNumber(abonoTxt),
                        onValueChange = { input ->
                            abonoTxt = input.filter(Char::isDigit)
                        },
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
                        val operacionSeleccionada = operacionCompraSeleccionada

                        if (operacionSeleccionada == null) {
                            Toast.makeText(
                                context,
                                "Debes seleccionar una operación activa",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (operacionSeleccionada.estado != "ACTIVA") {
                            Toast.makeText(
                                context,
                                "La operación seleccionada no está activa",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (cantidad <= 0) {
                            Toast.makeText(
                                context,
                                "La cantidad debe ser mayor a 0",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (precio <= 0) {
                            Toast.makeText(
                                context,
                                "El precio debe ser mayor a 0",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        vm.guardar(
                            CompraCafeImput(
                                productoNombre = producto,
                                unidad = unidad,
                                cantidad = cantidad,
                                precioUnitCompra = precio,
                                proveedor = proveedorTxt.ifBlank { null },
                                esCredito = esCredito,
                                nota = notaTxt.ifBlank { null },
                                abono = abono,
                                operacionCompraId = operacionSeleccionada.id
                            )
                        )

                        cantidadTxt = ""
                        precioTxt = ""
                        proveedorTxt = ""
                        notaTxt = ""
                        esCredito = false
                        abonoTxt = ""
                        //operacionCompraSeleccionada = null
                        expandedOperacion = false
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