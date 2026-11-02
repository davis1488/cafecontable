package com.ethandev.cafecontable.ui.screen.compras

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.constants.OPERACION_NUEVA_ID
import com.ethandev.cafecontable.domain.model.CompraCafeImput
import com.ethandev.cafecontable.ui.utils.formatNumber

private val CafeFondo = Color(0xFFF7F5EF)
private val CafeOscuro = Color(0xFF493628)
private val VerdeCafe = Color(0xFF376B4B)
private val VerdeClaro = Color(0xFFEAF2E9)
private val TextoSuave = Color(0xFF756B62)

fun parseCantidad(input: String): Double {
    val texto = input.replace(" ", "").replace(",", ".")
    if (texto.isBlank()) return 0.0
    return try {
        texto.split("+").filter { it.isNotBlank() }.sumOf { it.toDouble() }
    } catch (_: Exception) {
        0.0
    }
}

@Composable
private fun CompraSection(
    titulo: String,
    descripcion: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CafeOscuro)
            if (descripcion != null) {
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
            }
            content()
        }
    }
}

@Composable
private fun CompraValor(label: String, value: String, destacado: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (destacado) VerdeCafe else TextoSuave)
        Text(value, style = if (destacado) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = if (destacado) VerdeCafe else CafeOscuro)
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
    var expandedOperacion by remember { mutableStateOf(false) }

    val cantidad = parseCantidad(cantidadTxt)
    val precio = precioTxt.toLongOrNull() ?: 0L
    val abono = abonoTxt.toLongOrNull() ?: 0L
    val total = if (cantidad > 0.0 && precio > 0) (cantidad * precio).toLong() else 0L
    val saldo = total - abono
    val operacionesCompra = state.operacionesCompra

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
        containerColor = CafeFondo,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CafeOscuro)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFF67503E)) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null,
                                tint = Color(0xFFF5DEBA), modifier = Modifier.padding(12.dp).size(28.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nueva compra", color = Color.White, style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold)
                            Text("Registra café y controla su costo", color = Color(0xFFEADCCB),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                CompraSection("Datos del café", "Selecciona el producto y registra los kilogramos") {
                    ExposedDropdownMenuBox(expanded = expandedProducto, onExpandedChange = { expandedProducto = !expandedProducto }) {
                        OutlinedTextField(
                            value = producto, onValueChange = {}, readOnly = true,
                            label = { Text("Producto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProducto) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true
                        )
                        ExposedDropdownMenu(expanded = expandedProducto, onDismissRequest = { expandedProducto = false }) {
                            productos.forEach { opcion ->
                                DropdownMenuItem(text = { Text(opcion) }, onClick = { producto = opcion; expandedProducto = false })
                            }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = expandedUnidad, onExpandedChange = { expandedUnidad = !expandedUnidad }) {
                        OutlinedTextField(
                            value = unidad, onValueChange = {}, readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedUnidad) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true
                        )
                        ExposedDropdownMenu(expanded = expandedUnidad, onDismissRequest = { expandedUnidad = false }) {
                            unidades.forEach { opcion ->
                                DropdownMenuItem(text = { Text(opcion) }, onClick = { unidad = opcion; expandedUnidad = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = cantidadTxt,
                        onValueChange = { input -> cantidadTxt = input.filter { it.isDigit() || it == '+' } },
                        label = { Text("Cantidad ($unidad)") },
                        placeholder = { Text("Ej: 100+250+150") },
                        supportingText = { Text("Puedes sumar varias cantidades usando +") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Surface(color = VerdeClaro, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            CompraValor("Cantidad total", "${formatNumber(cantidad.toLong().toString())} $unidad", destacado = true)
                        }
                    }
                    OutlinedTextField(
                        value = formatNumber(precioTxt),
                        onValueChange = { input -> precioTxt = input.filter(Char::isDigit) },
                        label = { Text("Precio compra unitario (COP)") },
                        placeholder = { Text("Ej: 18000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            }

            item {
                CompraSection("Información de la operación", "Organiza la compra y sus observaciones") {
                    val opcionesOperacion = listOf(OPERACION_NUEVA_ID to "Nuevo") +
                            operacionesCompra.map { it.id to it.nombre }
                    val operacionCompraId = state.operacionCompraIdSeleccionada
                    val textoSeleccionado = if (operacionCompraId == OPERACION_NUEVA_ID) "Nuevo"
                    else operacionesCompra.firstOrNull { it.id == operacionCompraId }?.nombre ?: "Nuevo"
                    ExposedDropdownMenuBox(expanded = expandedOperacion, onExpandedChange = { expandedOperacion = !expandedOperacion }) {
                        OutlinedTextField(
                            value = textoSeleccionado, onValueChange = {}, readOnly = true,
                            label = { Text("Operación de compra") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedOperacion) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true
                        )
                        ExposedDropdownMenu(expanded = expandedOperacion, onDismissRequest = { expandedOperacion = false }) {
                            opcionesOperacion.forEach { (id, nombre) ->
                                DropdownMenuItem(text = { Text(nombre) }, onClick = {
                                    vm.seleccionarOperacionCompra(id)
                                    expandedOperacion = false
                                })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = proveedorTxt, onValueChange = { proveedorTxt = it },
                        label = { Text("Proveedor (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = notaTxt, onValueChange = { notaTxt = it },
                        label = { Text("Nota (opcional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2
                    )
                }
            }

            item {
                CompraSection("Forma de pago") {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Compra a crédito", fontWeight = FontWeight.SemiBold, color = CafeOscuro)
                            Text(if (esCredito) "Se registrará una cuenta por pagar" else "Pago de contado",
                                style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        }
                        Switch(checked = esCredito, onCheckedChange = { esCredito = it })
                    }
                    if (esCredito) {
                        OutlinedTextField(
                            value = formatNumber(abonoTxt),
                            onValueChange = { input -> abonoTxt = input.filter(Char::isDigit) },
                            label = { Text("Abono al crédito (COP)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        CompraValor("Saldo pendiente", "${formatNumber(saldo.toString())} COP")
                    }
                }
            }

            item {
                Surface(color = VerdeClaro, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("RESUMEN DE COMPRA", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold, color = VerdeCafe)
                        CompraValor("Cantidad", "${formatNumber(cantidad.toLong().toString())} $unidad")
                        CompraValor("Precio unitario", "$ ${formatNumber(precio.toString())}")
                        HorizontalDivider(color = Color(0xFFC8DEC9))
                        CompraValor("Total de compra", "$ ${formatNumber(total.toString())}", destacado = true)
                        if (esCredito) CompraValor("Saldo crédito", "$ ${formatNumber(saldo.toString())}")
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val operacionSeleccionadaId = state.operacionCompraIdSeleccionada
                        if (operacionSeleccionadaId.isNullOrBlank()) {
                            Toast.makeText(context, "Debes seleccionar una operación", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (cantidad <= 0.0) {
                            Toast.makeText(context, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (precio <= 0L) {
                            Toast.makeText(context, "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        vm.guardar(
                            CompraCafeImput(
                                productoNombre = producto, unidad = unidad, cantidad = cantidad,
                                precioUnitCompra = precio, proveedor = proveedorTxt.ifBlank { null },
                                esCredito = esCredito, nota = notaTxt.ifBlank { null },
                                abono = abono, operacionCompraId = operacionSeleccionadaId
                            )
                        )
                        cantidadTxt = ""
                        precioTxt = ""
                        proveedorTxt = ""
                        notaTxt = ""
                        esCredito = false
                        abonoTxt = ""
                        expandedOperacion = false
                    },
                    enabled = !state.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeCafe),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(Icons.Filled.LocalCafe, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.loading) "Guardando..." else "Guardar compra", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
