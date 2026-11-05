package com.ethandev.cafecontable.ui.screen.asignacionmezcla

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.NumberFormat
import java.util.Locale

private val FondoAsignacion = Color(0xFFF7F5EF)
private val CafeAsignacion = Color(0xFF493628)
private val VerdeAsignacion = Color(0xFF376B4B)
private val TextoAsignacion = Color(0xFF29231E)
private val SecundarioAsignacion = Color(0xFF665C53)
private val DoradoAsignacion = Color(0xFFF4D9AF)
private val RojoAsignacion = Color(0xFFC62828)
private val ClaroAsignacion = Color(0xFFE7EEE7)
private val BordeAsignacion = Color(0xFFB1A69B)

private fun kgAsignacion(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 2
    }.format(valor) + " kg"

private fun copAsignacion(valor: Long): String =
    "$ " + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(valor)

@Composable
fun AsignacionMezclaPedidoScreen(viewModel: AsignacionMezclaPedidoViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var snackbarError by remember { mutableStateOf(false) }

    var pedidoId by remember { mutableStateOf("") }
    var mezclaId by remember { mutableStateOf("") }
    var cantidadTxt by remember { mutableStateOf("") }
    var factorTxt by remember { mutableStateOf("90") }
    var nota by remember { mutableStateOf("") }
    var anunciosAbiertos by remember { mutableStateOf(true) }
    var mezclasAbiertas by remember { mutableStateOf(true) }
    var detallesAbiertos by remember { mutableStateOf(false) }

    // Se vuelve a consultar al abrir o regresar a esta pantalla.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.cargarDatos()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.okMsg, state.error) {
        val error = state.error
        val exito = state.okMsg
        if (error != null || exito != null) {
            snackbarError = error != null
            viewModel.limpiarMensajes()
            if (error == null && exito != null) {
                pedidoId = ""
                mezclaId = ""
                cantidadTxt = ""
                factorTxt = "90"
                nota = ""
                anunciosAbiertos = true
                mezclasAbiertas = true
                detallesAbiertos = false
            }
            snackbarHost.showSnackbar(error ?: exito.orEmpty(), withDismissAction = true)
        }
    }

    val pedido = state.pedidosDisponibles.firstOrNull { it.pedidoId == pedidoId }
    val mezcla = state.mezclasDisponibles.firstOrNull { it.mezclaId == mezclaId }
    val faltante = ((pedido?.cantidadPedido ?: 0.0) - (pedido?.cantidadAsignada ?: 0.0)).coerceAtLeast(0.0)
    val disponible = mezcla?.cantidadDisponible ?: 0.0
    val cantidad = cantidadTxt.toDoubleOrNull() ?: 0.0
    val precio = pedido?.precioUnitVenta ?: 0L
    val factor = factorTxt.toDoubleOrNull()
    val factorValido = factor != null && factor.isFinite() && factor > 0.0
    val puedeGuardar = !state.loading && !state.loadingDatos && pedido != null && mezcla != null &&
            cantidad.isFinite() && cantidad > 0.0 && cantidad <= faltante &&
            cantidad <= disponible && factorValido && precio > 0L &&
            (mezcla.estado.equals("ENTREGADO", true) || mezcla.estado.equals("ANALIZADO", true))
    val maximo = minOf(faltante, disponible)
    val subtotal = if (cantidad.isFinite() && cantidad > 0 && precio > 0) {
        viewModel.calcularSubtotal(cantidad, precio)
    } else 0L

    Scaffold(
        containerColor = FondoAsignacion,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHost) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (snackbarError) RojoAsignacion else VerdeAsignacion,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(color = CafeAsignacion, shape = RoundedCornerShape(18.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Link, contentDescription = null, tint = DoradoAsignacion)
                    Column(Modifier.weight(1f)) {
                        Text("Asignaciones", color = Color.White,
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Vincula mezclas a anuncios", color = DoradoAsignacion,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { viewModel.cargarDatos() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar datos", tint = Color.White)
                    }
                }
            }

            if (state.loadingDatos) LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(), color = VerdeAsignacion)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniResumen("Anuncios", state.pedidosDisponibles.count { it.cantidadPedido > it.cantidadAsignada }, Modifier.weight(1f))
                MiniResumen("Mezclas", state.mezclasDisponibles.count { it.cantidadDisponible > 0.0 }, Modifier.weight(1f))
            }

            SeccionSeleccion(
                titulo = "1. Anuncio de venta",
                detalle = pedido?.let { "${it.clienteNombre} · faltan ${kgAsignacion(faltante)}" }
                    ?: "Selecciona el anuncio por completar",
                abierto = anunciosAbiertos,
                seleccionado = pedido != null,
                onClick = { anunciosAbiertos = !anunciosAbiertos }
            )
            if (anunciosAbiertos) {
                if (!state.loadingDatos && state.pedidosDisponibles.none { it.cantidadPedido > it.cantidadAsignada }) {
                    MensajeAsignacion("No hay anuncios pendientes de asignación.")
                }
                state.pedidosDisponibles.filter { it.cantidadPedido > it.cantidadAsignada }.forEach { actual ->
                    val restante = (actual.cantidadPedido - actual.cantidadAsignada).coerceAtLeast(0.0)
                    val seleccionado = pedidoId == actual.pedidoId
                    Surface(
                        color = if (seleccionado) ClaroAsignacion else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            pedidoId = actual.pedidoId
                            cantidadTxt = ""
                            anunciosAbiertos = false
                        }
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Storefront, null, tint = VerdeAsignacion,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(actual.clienteNombre, modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold, color = CafeAsignacion,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (seleccionado) Icon(Icons.Filled.CheckCircle, "Seleccionado", tint = VerdeAsignacion)
                            }
                            DatoAsignacionCompacto("Pendiente", kgAsignacion(restante))
                            DatoAsignacionCompacto("Pactado / asignado",
                                "${kgAsignacion(actual.cantidadPedido)} / ${kgAsignacion(actual.cantidadAsignada)}")
                            DatoAsignacionCompacto("Precio de venta", copAsignacion(actual.precioUnitVenta) + " / kg")
                            LinearProgressIndicator(
                                progress = if (actual.cantidadPedido > 0)
                                    (actual.cantidadAsignada / actual.cantidadPedido).toFloat().coerceIn(0f, 1f) else 0f,
                                modifier = Modifier.fillMaxWidth().height(5.dp),
                                color = VerdeAsignacion, trackColor = ClaroAsignacion
                            )
                        }
                    }
                }
            }

            SeccionSeleccion(
                titulo = "2. Mezcla disponible",
                detalle = mezcla?.let { "${it.descripcion} · ${kgAsignacion(disponible)}" }
                    ?: "Selecciona la mezcla entregada",
                abierto = mezclasAbiertas,
                seleccionado = mezcla != null,
                onClick = { mezclasAbiertas = !mezclasAbiertas }
            )
            if (mezclasAbiertas) {
                val disponibles = state.mezclasDisponibles.filter {
                    it.cantidadDisponible > 0.0 &&
                            (it.estado.equals("ENTREGADO", true) || it.estado.equals("ANALIZADO", true))
                }
                if (!state.loadingDatos && disponibles.isEmpty()) {
                    MensajeAsignacion("No hay mezclas entregadas con saldo disponible.")
                }
                disponibles.forEach { actual ->
                    val seleccionado = mezclaId == actual.mezclaId
                    Surface(
                        color = if (seleccionado) ClaroAsignacion else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            mezclaId = actual.mezclaId
                            cantidadTxt = ""
                            mezclasAbiertas = false
                        }
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Inventory2, null, tint = VerdeAsignacion,
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(actual.descripcion, modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold, color = CafeAsignacion,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                                if (seleccionado) Icon(Icons.Filled.CheckCircle, "Seleccionada", tint = VerdeAsignacion)
                            }
                            DatoAsignacionCompacto("Disponible", kgAsignacion(actual.cantidadDisponible))
                            DatoAsignacionCompacto("Costo promedio", copAsignacion(actual.costoPromedioKg) + " / kg")
                            Text(actual.estado, color = VerdeAsignacion, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Surface(color = Color.White, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. Registrar asignación", color = CafeAsignacion,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (pedido == null || mezcla == null) {
                        Text("Selecciona un anuncio y una mezcla para continuar.",
                            color = SecundarioAsignacion, style = MaterialTheme.typography.bodySmall)
                    } else {
                        DatoAsignacionCompacto("Pendiente del anuncio", kgAsignacion(faltante))
                        DatoAsignacionCompacto("Saldo de la mezcla", kgAsignacion(disponible))
                    }

                    OutlinedTextField(
                        value = cantidadTxt,
                        onValueChange = { cantidadTxt = it.replace(',', '.') },
                        label = { Text("Cantidad a asignar (kg)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = coloresAsignacion()
                    )
                    if (pedido != null && mezcla != null && maximo > 0.0) {
                        TextButton(
                            onClick = { cantidadTxt = maximo.toString() },
                            colors = ButtonDefaults.textButtonColors(contentColor = VerdeAsignacion),
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Usar máximo: ${kgAsignacion(maximo)}") }
                    }
                    if (cantidad > 0 && mezcla != null && cantidad > disponible) {
                        Text("La cantidad supera el saldo de la mezcla.", color = RojoAsignacion)
                    } else if (cantidad > 0 && pedido != null && cantidad > faltante) {
                        Text("La cantidad supera lo pendiente del anuncio.", color = RojoAsignacion)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = factorTxt,
                            onValueChange = { factorTxt = it.replace(',', '.') },
                            label = { Text("Factor") },
                            modifier = Modifier.weight(1f), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = coloresAsignacion()
                        )
                        Surface(color = ClaroAsignacion, shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)) {
                            Column(Modifier.padding(10.dp)) {
                                Text("Precio / kg", color = SecundarioAsignacion,
                                    style = MaterialTheme.typography.labelSmall)
                                Text(if (pedido != null) copAsignacion(precio) else "—",
                                    color = CafeAsignacion, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (!factorValido) Text("El factor debe ser mayor a cero.", color = RojoAsignacion)

                    Surface(color = ClaroAsignacion, shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Subtotal", fontWeight = FontWeight.SemiBold, color = CafeAsignacion)
                            Text(copAsignacion(subtotal), fontWeight = FontWeight.Bold,
                                color = VerdeAsignacion, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    TextButton(
                        onClick = { detallesAbiertos = !detallesAbiertos },
                        colors = ButtonDefaults.textButtonColors(contentColor = CafeAsignacion)
                    ) {
                        Text(if (detallesAbiertos) "Ocultar nota" else "Agregar nota (opcional)")
                        Spacer(Modifier.width(4.dp))
                        Icon(if (detallesAbiertos) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null)
                    }
                    if (detallesAbiertos) {
                        OutlinedTextField(
                            value = nota, onValueChange = { nota = it },
                            label = { Text("Nota") }, modifier = Modifier.fillMaxWidth(),
                            minLines = 2, colors = coloresAsignacion()
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.asignar(
                                pedidoId = pedidoId, mezclaId = mezclaId,
                                cantidadAsignada = cantidad, precioUnitVenta = precio,
                                factor = factor ?: 90.0, nota = nota
                            )
                        },
                        enabled = puedeGuardar,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeAsignacion),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (state.loading) "Guardando…" else "Guardar asignación")
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SeccionSeleccion(
    titulo: String, detalle: String, abierto: Boolean,
    seleccionado: Boolean, onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.White,
        shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(titulo, color = CafeAsignacion, fontWeight = FontWeight.Bold)
                Text(detalle, color = if (seleccionado) VerdeAsignacion else SecundarioAsignacion,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (seleccionado) Icon(Icons.Filled.CheckCircle, null, tint = VerdeAsignacion,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Icon(if (abierto) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (abierto) "Contraer" else "Expandir", tint = CafeAsignacion)
        }
    }
}

@Composable
private fun MiniResumen(nombre: String, cantidad: Int, modifier: Modifier) {
    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(cantidad.toString(), color = VerdeAsignacion, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            Text(nombre, color = SecundarioAsignacion, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DatoAsignacionCompacto(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(etiqueta, modifier = Modifier.weight(1f), color = SecundarioAsignacion,
            style = MaterialTheme.typography.bodySmall)
        Text(valor, color = TextoAsignacion, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MensajeAsignacion(texto: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Text(texto, Modifier.padding(16.dp), color = SecundarioAsignacion)
    }
}

@Composable
private fun coloresAsignacion() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextoAsignacion,
    unfocusedTextColor = TextoAsignacion,
    focusedLabelColor = CafeAsignacion,
    unfocusedLabelColor = CafeAsignacion,
    focusedBorderColor = VerdeAsignacion,
    unfocusedBorderColor = BordeAsignacion,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = VerdeAsignacion
)
