package com.ethandev.cafecontable.ui.screen.asignacionmezcla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

private val FondoAsignacion = Color(0xFFF7F5EF)
private val CafeAsignacion = Color(0xFF493628)
private val VerdeAsignacion = Color(0xFF376B4B)
private val TextoSecundario = Color(0xFF786D63)
private val DoradoAsignacion = Color(0xFFF4D9AF)

private fun kilosAsignacion(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 2
    }.format(valor) + " kg"

private fun pesosAsignacion(valor: Long): String =
    "$" + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(valor)

@Composable
fun AsignacionMezclaPedidoScreen(
    viewModel: AsignacionMezclaPedidoViewModel
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var esError by remember { mutableStateOf(false) }

    var pedidoId by remember { mutableStateOf("") }
    var mezclaId by remember { mutableStateOf("") }
    var cantidadAsignada by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("90") }
    var nota by remember { mutableStateOf("") }

    val pedido = state.pedidosDisponibles.firstOrNull { it.pedidoId == pedidoId }
    val mezcla = state.mezclasDisponibles.firstOrNull { it.mezclaId == mezclaId }
    val faltante = ((pedido?.cantidadPedido ?: 0.0) -
            (pedido?.cantidadAsignada ?: 0.0)).coerceAtLeast(0.0)
    val disponible = mezcla?.cantidadDisponible ?: 0.0
    val cantidad = cantidadAsignada.replace(',', '.').toDoubleOrNull() ?: 0.0
    val precio = pedido?.precioUnitVenta ?: 0L
    val subtotal = viewModel.calcularSubtotal(cantidad, precio)
    val factorValido = (factor.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0
    val puedeGuardar = !state.loading && !state.loadingDatos && pedido != null &&
            mezcla != null && cantidad > 0.0 && cantidad <= disponible &&
            cantidad <= faltante && factorValido

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    LaunchedEffect(state.okMsg, state.error) {
        val error = state.error
        val exito = state.okMsg
        if (error != null || exito != null) {
            esError = error != null
            // Limpiar antes de suspenderse en showSnackbar evita mostrarlo otra vez.
            viewModel.limpiarMensajes()
            if (exito != null && error == null) {
                pedidoId = ""
                mezclaId = ""
                cantidadAsignada = ""
                factor = "90"
                nota = ""
            }
            snackbarHostState.showSnackbar(
                message = error ?: exito.orEmpty(),
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        containerColor = FondoAsignacion,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (esError) Color(0xFF8E382F) else VerdeAsignacion,
                    contentColor = Color.White,
                    actionColor = Color.White,
                    dismissActionContentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = CafeAsignacion,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Link, contentDescription = null,
                        tint = DoradoAsignacion, modifier = Modifier.size(32.dp))
                    Column {
                        Text("Asignación de mezclas", color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text("Vincula el café disponible a un anuncio",
                            color = Color(0xFFEBDAC8),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (state.loadingDatos) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                    color = VerdeAsignacion)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()) {
                TarjetaResumenAsignacion(
                    "Anuncios", state.pedidosDisponibles.size.toString(), Modifier.weight(1f)
                )
                TarjetaResumenAsignacion(
                    "Mezclas", state.mezclasDisponibles.size.toString(), Modifier.weight(1f)
                )
            }

            Text("1. Selecciona un anuncio", color = CafeAsignacion,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)

            if (!state.loadingDatos && state.pedidosDisponibles.isEmpty()) {
                MensajeVacioAsignacion("No hay anuncios disponibles para asignación.")
            }

            state.pedidosDisponibles.forEach { actual ->
                val pendiente = (actual.cantidadPedido - actual.cantidadAsignada)
                    .coerceAtLeast(0.0)
                val seleccionado = pedidoId == actual.pedidoId
                val completado = pendiente <= 0.0
                TarjetaAsignacion(seleccionado = seleccionado) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Icon(Icons.Filled.Storefront, null, tint = VerdeAsignacion)
                        Text(actual.clienteNombre,
                            color = CafeAsignacion,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        if (seleccionado) {
                            Icon(Icons.Filled.CheckCircle, "Seleccionado",
                                tint = VerdeAsignacion)
                        }
                    }
                    Text(actual.productoNombre, color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall)
                    DatoAsignacion("Pactado", kilosAsignacion(actual.cantidadPedido))
                    DatoAsignacion("Asignado", kilosAsignacion(actual.cantidadAsignada))
                    DatoAsignacion("Pendiente", kilosAsignacion(pendiente))
                    DatoAsignacion("Precio venta / kg", pesosAsignacion(actual.precioUnitVenta))
                    LinearProgressIndicator(
                        progress = if (actual.cantidadPedido > 0) {
                            (actual.cantidadAsignada / actual.cantidadPedido)
                                .toFloat().coerceIn(0f, 1f)
                        } else 0f,
                        modifier = Modifier.fillMaxWidth().height(7.dp),
                        color = VerdeAsignacion,
                        trackColor = Color(0xFFE5ECE5)
                    )
                    Button(
                        onClick = { pedidoId = actual.pedidoId },
                        enabled = !completado && !seleccionado,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeAsignacion),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(when {
                            seleccionado -> "Anuncio seleccionado"
                            completado -> "Anuncio completado"
                            else -> "Seleccionar anuncio"
                        })
                    }
                }
            }

            Text("2. Selecciona una mezcla", color = CafeAsignacion,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)

            if (!state.loadingDatos && state.mezclasDisponibles.isEmpty()) {
                MensajeVacioAsignacion("No hay mezclas disponibles para asignación.")
            }

            state.mezclasDisponibles.forEach { actual ->
                val seleccionado = mezclaId == actual.mezclaId
                val agotada = actual.cantidadDisponible <= 0.0
                TarjetaAsignacion(seleccionado = seleccionado) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Icon(Icons.Filled.Inventory2, null, tint = VerdeAsignacion)
                        Text(actual.descripcion, color = CafeAsignacion,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        if (seleccionado) {
                            Icon(Icons.Filled.CheckCircle, "Seleccionada",
                                tint = VerdeAsignacion)
                        }
                    }
                    Text("Estado: ${actual.estado}", color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall)
                    DatoAsignacion("Cantidad total", kilosAsignacion(actual.cantidadTotal))
                    DatoAsignacion("Disponible", kilosAsignacion(actual.cantidadDisponible))
                    DatoAsignacion("Costo promedio / kg", pesosAsignacion(actual.costoPromedioKg))
                    Button(
                        onClick = { mezclaId = actual.mezclaId },
                        enabled = !agotada && !seleccionado,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CafeAsignacion),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(when {
                            seleccionado -> "Mezcla seleccionada"
                            agotada -> "Mezcla agotada"
                            else -> "Seleccionar mezcla"
                        })
                    }
                }
            }

            Text("3. Datos de la asignación", color = CafeAsignacion,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)

            TarjetaAsignacion(seleccionado = false) {
                DatoAsignacion("Anuncio", pedido?.clienteNombre ?: "Sin seleccionar")
                DatoAsignacion("Mezcla", mezcla?.descripcion ?: "Sin seleccionar")
                if (pedido != null && mezcla != null) {
                    DatoAsignacion("Pendiente de anuncio", kilosAsignacion(faltante))
                    DatoAsignacion("Disponible de mezcla", kilosAsignacion(disponible))
                }
                OutlinedTextField(
                    value = cantidadAsignada,
                    onValueChange = { cantidadAsignada = it.replace(',', '.') },
                    label = { Text("Cantidad asignada (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (cantidad > disponible && mezcla != null) {
                    Text("Supera la cantidad disponible de la mezcla.",
                        color = Color(0xFFB3261E))
                } else if (cantidad > faltante && pedido != null) {
                    Text("Supera lo pendiente del anuncio.",
                        color = Color(0xFFB3261E))
                }
                OutlinedTextField(
                    value = if (pedido != null) pesosAsignacion(precio) else "",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Precio de venta / kg") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = factor,
                    onValueChange = { factor = it.replace(',', '.') },
                    label = { Text("Factor") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (!factorValido) {
                    Text("Ingresa un factor válido mayor a cero.",
                        color = Color(0xFFB3261E))
                }
                Surface(color = Color(0xFFEAF2E9),
                    shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Subtotal", color = VerdeAsignacion,
                            fontWeight = FontWeight.SemiBold)
                        Text(pesosAsignacion(subtotal), color = VerdeAsignacion,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
                OutlinedTextField(
                    value = nota, onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2
                )
                Button(
                    onClick = {
                        viewModel.asignar(
                            pedidoId = pedidoId,
                            mezclaId = mezclaId,
                            cantidadAsignada = cantidad,
                            precioUnitVenta = precio,
                            factor = factor.replace(',', '.').toDoubleOrNull() ?: 90.0,
                            nota = nota
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = puedeGuardar,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeAsignacion),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White, strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (state.loading) "Guardando..." else "Guardar asignación")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TarjetaResumenAsignacion(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, color = Color.White,
        shape = RoundedCornerShape(17.dp), shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, color = VerdeAsignacion, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall)
            Text(titulo, color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TarjetaAsignacion(
    seleccionado: Boolean,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) Color(0xFFF0F7EF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = contenido
        )
    }
}

@Composable
private fun DatoAsignacion(titulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top) {
        Text(titulo, color = TextoSecundario,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(valor, color = CafeAsignacion, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MensajeVacioAsignacion(mensaje: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()) {
        Text(mensaje, modifier = Modifier.padding(20.dp), color = TextoSecundario)
    }
}
