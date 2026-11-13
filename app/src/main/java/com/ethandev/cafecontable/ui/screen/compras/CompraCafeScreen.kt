package com.ethandev.cafecontable.ui.screen.compras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.core.utils.FechaUtils
import com.ethandev.cafecontable.domain.constants.OPERACION_NUEVA_ID
import com.ethandev.cafecontable.domain.model.CompraCafeImput
import com.ethandev.cafecontable.ui.utils.formatNumber
import java.text.NumberFormat
import java.util.Locale

private val CompraCafe = Color(0xFF493628)
private val CompraVerde = Color(0xFF376B4B)
private val CompraFondo = Color(0xFFF7F5EF)
private val CompraSuave = Color(0xFF665C53)
private val CompraVerdeClaro = Color(0xFFE7EEE7)
private val CompraBorde = Color(0xFF9B8D80)
private val CompraRojo = Color(0xFFC62828)

// Se conserva como función pública: otros archivos podrían reutilizarla.
fun parseCantidad(input: String): Double {
    val texto = input.replace(" ", "").replace(",", ".")
    if (texto.isBlank()) return 0.0
    return try {
        texto.split("+").filter { it.isNotBlank() }.sumOf { it.toDouble() }
    } catch (_: Exception) {
        0.0
    }
}

private fun cantidadTexto(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 2
    }.format(valor)

private fun dineroTexto(valor: Long): String = "$ ${formatNumber(valor.toString())}"

@Composable
private fun compraColoresCampo() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black,
    focusedLabelColor = CompraVerde,
    unfocusedLabelColor = CompraCafe,
    disabledLabelColor = CompraCafe,
    focusedPlaceholderColor = CompraSuave,
    unfocusedPlaceholderColor = CompraSuave,
    focusedBorderColor = CompraVerde,
    unfocusedBorderColor = CompraBorde,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = CompraVerde
)

@Composable
private fun SeccionCompraCompacta(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(17.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                titulo,
                color = CompraCafe,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            contenido()
        }
    }
}

@Composable
private fun DatoCompraCompacto(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            etiqueta,
            modifier = Modifier.weight(1f),
            color = if (destacado) CompraVerde else CompraSuave,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            valor,
            color = if (destacado) CompraVerde else CompraCafe,
            fontWeight = FontWeight.Bold,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompraCafeScreen(vm: CompraCafeViewModel) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current.applicationContext

    val sincronizador = remember(context) {
        AppModule.provideSincronizarComprasPendientes(context)
    }

    var sincronizando by remember {
        mutableStateOf(false)
    }

    val productos = remember { listOf("Cafe", "Pasilla", "Regular") }

    var producto by remember { mutableStateOf(productos.first()) }
    val unidad = "KG" // Actualmente es la única unidad disponible: evitamos un selector innecesario.
    var cantidadTxt by remember { mutableStateOf("") }
    var precioTxt by remember { mutableStateOf("") }
    var proveedorTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }
    var esCredito by remember { mutableStateOf(false) }
    var abonoTxt by remember { mutableStateOf("") }
    var productoExpandido by remember { mutableStateOf(false) }
    var operacionExpandida by remember { mutableStateOf(false) }
    var detallesExpandidos by remember { mutableStateOf(false) }
    var guardadoSolicitado by remember { mutableStateOf(false) }

    val cantidad = parseCantidad(cantidadTxt)
    val precio = precioTxt.toLongOrNull() ?: 0L
    val abono = abonoTxt.toLongOrNull() ?: 0L
    val total = if (cantidad > 0.0 && precio > 0L) (cantidad * precio).toLong() else 0L
    val saldo = total - abono
    val operaciones = state.operacionesCompra
    val operacionId = state.operacionCompraIdSeleccionada
    val operacionVisible = if (operacionId == OPERACION_NUEVA_ID) "Nuevo" else
        operaciones.firstOrNull { it.id == operacionId }?.let {
            FechaUtils.formatearNombreOperacion(it.nombre)
        } ?: "Nuevo"

    LaunchedEffect(state.error, state.okMsg) {
        val error = state.error
        val ok = state.okMsg
        if (error != null || ok != null) {
            if (error != null) guardadoSolicitado = false
            if (ok != null && error == null && guardadoSolicitado) {
                cantidadTxt = ""
                precioTxt = ""
                proveedorTxt = ""
                notaTxt = ""
                abonoTxt = ""
                esCredito = false
                detallesExpandidos = false
                guardadoSolicitado = false
            }
            vm.limpiarMensajes()
            snackbarHostState.showSnackbar(error ?: ok.orEmpty())
        }
    }

    Scaffold(
        containerColor = CompraFondo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            item {
                Surface(color = CompraCafe, shape = RoundedCornerShape(17.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.ShoppingCart, null, tint = Color(0xFFF4D9AF),
                            modifier = Modifier.size(27.dp))
                        Column {
                            Text("Nueva compra", color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("Café, cantidades y costo", color = Color(0xFFEBDAC8),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                SeccionCompraCompacta("Datos del café") {
                    ExposedDropdownMenuBox(
                        expanded = productoExpandido,
                        onExpandedChange = { productoExpandido = !productoExpandido }
                    ) {
                        OutlinedTextField(
                            value = producto,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Producto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(productoExpandido) },
                            colors = compraColoresCampo(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = productoExpandido,
                            onDismissRequest = { productoExpandido = false }
                        ) {
                            productos.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = { producto = opcion; productoExpandido = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cantidadTxt,
                        onValueChange = { cantidadTxt = it.filter { c -> c.isDigit() || c == '+' } },
                        label = { Text("Cantidad ($unidad)") },
                        placeholder = { Text("100+250+150") },
                        supportingText = { Text("Puedes sumar cantidades con +") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = compraColoresCampo(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().background(CompraVerdeClaro, RoundedCornerShape(11.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cantidad total", color = CompraCafe,
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("${cantidadTexto(cantidad)} kg", color = CompraVerde,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium)
                    }

                    OutlinedTextField(
                        value = formatNumber(precioTxt),
                        onValueChange = { precioTxt = it.filter(Char::isDigit) },
                        label = { Text("Precio por kg (COP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = compraColoresCampo(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            item {
                SeccionCompraCompacta("Operación y pago") {
                    ExposedDropdownMenuBox(
                        expanded = operacionExpandida,
                        onExpandedChange = { operacionExpandida = !operacionExpandida }
                    ) {
                        OutlinedTextField(
                            value = operacionVisible,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Operación de compra") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(operacionExpandida) },
                            colors = compraColoresCampo(),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = operacionExpandida,
                            onDismissRequest = { operacionExpandida = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nuevo") },
                                onClick = {
                                    vm.seleccionarOperacionCompra(OPERACION_NUEVA_ID)
                                    operacionExpandida = false
                                }
                            )
                            operaciones.forEach { operacion ->
                                DropdownMenuItem(
                                    text = {
                                        Text(FechaUtils.formatearNombreOperacion(operacion.nombre),
                                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    },
                                    onClick = {
                                        vm.seleccionarOperacionCompra(operacion.id)
                                        operacionExpandida = false
                                    }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Compra a crédito", color = CompraCafe,
                                fontWeight = FontWeight.SemiBold)
                            Text(if (esCredito) "Genera una cuenta por pagar" else "Pago de contado",
                                color = CompraSuave, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = esCredito,
                            onCheckedChange = {
                                esCredito = it
                                if (it) detallesExpandidos = true
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White,
                                checkedTrackColor = CompraVerde)
                        )
                    }
                    if (esCredito) {
                        OutlinedTextField(
                            value = formatNumber(abonoTxt),
                            onValueChange = { abonoTxt = it.filter(Char::isDigit) },
                            label = { Text("Abono inicial (COP)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = compraColoresCampo(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        DatoCompraCompacto("Saldo crédito", dineroTexto(saldo.coerceAtLeast(0L)))
                        if (abono > total && total > 0L) {
                            Text("El abono no puede superar el valor de la compra.",
                                color = CompraRojo, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    TextButton(
                        onClick = { detallesExpandidos = !detallesExpandidos },
                        colors = ButtonDefaults.textButtonColors(contentColor = CompraCafe),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Text(if (detallesExpandidos) "Ocultar proveedor y nota" else "Agregar proveedor o nota")
                    }
                    if (detallesExpandidos) {
                        OutlinedTextField(
                            value = proveedorTxt,
                            onValueChange = { proveedorTxt = it },
                            label = {
                                Text(
                                    if (esCredito)
                                        "Proveedor *"
                                    else
                                        "Proveedor (opcional)"
                                )
                            },
                            isError = esCredito && proveedorTxt.isBlank(),
                            supportingText = {
                                if (esCredito && proveedorTxt.isBlank()) {
                                    Text(
                                        text = "Campo obligatorio para compras a crédito",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            colors = compraColoresCampo(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = notaTxt,
                            onValueChange = { notaTxt = it },
                            label = { Text("Nota (opcional)") },
                            colors = compraColoresCampo(),
                            modifier = Modifier.fillMaxWidth(), minLines = 2
                        )
                    }
                }
            }

            item {
                Surface(color = CompraVerdeClaro, shape = RoundedCornerShape(17.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("RESUMEN DE COMPRA", color = CompraVerde,
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        DatoCompraCompacto("Cantidad", "${cantidadTexto(cantidad)} kg")
                        DatoCompraCompacto("Precio unitario", dineroTexto(precio))
                        HorizontalDivider(color = Color(0xFFC8DEC9))
                        DatoCompraCompacto("Total", dineroTexto(total), destacado = true)
                        if (esCredito) DatoCompraCompacto("Pendiente", dineroTexto(saldo.coerceAtLeast(0L)))
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val seleccionado = state.operacionCompraIdSeleccionada
                        val mensaje = when {
                            seleccionado.isNullOrBlank() -> "Debes seleccionar una operación"
                            cantidad <= 0.0 -> "La cantidad debe ser mayor a cero"
                            precio <= 0L -> "El precio debe ser mayor a cero"
                            esCredito && abono > total -> "El abono supera el total de la compra"
                            esCredito && proveedorTxt.isBlank() -> "Debes ingresar el proveedor para compras a crédito"
                            else -> null
                        }
                        if (mensaje != null) {
                            // Mostramos la validación sin depender de Toast.
                            scope.launch { snackbarHostState.showSnackbar(mensaje) }
                        } else {
                            guardadoSolicitado = true
                            vm.guardar(
                                CompraCafeImput(
                                    productoNombre = producto,
                                    unidad = unidad,
                                    cantidad = cantidad,
                                    precioUnitCompra = precio,
                                    proveedor = proveedorTxt.ifBlank { null },
                                    esCredito = esCredito,
                                    nota = notaTxt.ifBlank { null },
                                    abono = if (esCredito) abono else 0L,
                                    operacionCompraId = seleccionado!!
                                )
                            )
                        }
                    },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().height(49.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CompraVerde),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (state.loading) "Guardando..." else "Guardar compra",
                        fontWeight = FontWeight.SemiBold)
                }
            }


            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            sincronizando = true

                            try {
                                val resultado = sincronizador()

                                val mensaje = """
                        Sincronizadas: ${resultado.sincronizadas}
                        Pendientes: ${resultado.pendientes}
                        Conflictos: ${resultado.conflictos}
                    """.trimIndent()

                                snackbarHostState.showSnackbar(mensaje)

                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e

                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    "No se pudo sincronizar: ${e.message}"
                                )

                            } finally {
                                sincronizando = false
                            }
                        }
                    },
                    enabled = !sincronizando && !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CompraVerde
                    )
                ) {
                    if (sincronizando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = CompraVerde
                        )

                        Spacer(Modifier.width(8.dp))
                    }

                    Text(
                        text = if (sincronizando)
                            "Sincronizando..."
                        else
                            "Sincronizar compras",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

        }
    }
}
