package com.ethandev.cafecontable.ui.screen.ventaspedido

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ethandev.cafecontable.domain.constants.EstadoAnuncio
import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.dto.VentaPedidoInput
import com.ethandev.cafecontable.ui.utils.formatNumber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Fondo = Color(0xFFF7F5EF)
private val Cafe = Color(0xFF493628)
private val Verde = Color(0xFF376B4B)
private val Suave = Color(0xFF786D63)
private val Dorado = Color(0xFFF4D9AF)

private fun pesos(valor: Long): String =
    "$" + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(valor)

private fun kilos(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 2
    }.format(valor)

private enum class FiltroAnuncio(val titulo: String, val estado: EstadoAnuncio?) {
    TODOS("Todos", null),
    CREADOS("Creados", EstadoAnuncio.CREADO),
    PARCIALES("Parciales", EstadoAnuncio.ENTREGA_PARCIAL),
    TOTALES("Entregados", EstadoAnuncio.ENTREGA_TOTAL)
}

@Composable
fun VentasPedidoScreen(
    vm: VentasPedidoViewModel,
    onIrAsignacionMezcla: (String) -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mensajeError by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    var cliente by remember { mutableStateOf("") }
    var cantidadTxt by remember { mutableStateOf("") }
    var precioTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }
    var guardandoAnuncio by remember { mutableStateOf(false) }

    // Refrescar al entrar o volver desde Asignaciones, sin reiniciar la app.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, vm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.cargarVentas()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.error, state.okMsg) {
        val error = state.error
        val exito = state.okMsg
        if (error != null || exito != null) {
            mensajeError = error != null
            if (exito == "Venta registrada correctamente" && guardandoAnuncio) {
                cliente = ""
                cantidadTxt = ""
                precioTxt = ""
                notaTxt = ""
                tab = 1
            }
            guardandoAnuncio = false
            vm.limpiarMensajes()
            snackbarHostState.showSnackbar(
                message = error ?: exito.orEmpty(),
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }

    val cantidad = cantidadTxt.toDoubleOrNull() ?: 0.0
    val precio = precioTxt.toLongOrNull() ?: 0L
    val total = if (cantidad > 0 && precio > 0) (cantidad * precio).toLong() else 0L

    Scaffold(
        containerColor = Fondo,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (mensajeError) Color(0xFF8C4430) else Verde,
                    contentColor = Color.White,
                    actionColor = Dorado,
                    dismissActionContentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Fondo)
        ) {
            // Cabecera compacta: el historial prioriza el espacio para los anuncios.
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                if (tab == 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Cafe
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.ReceiptLong, null, tint = Dorado, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Anuncios de venta", color = Color.White,
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Pedidos, mezclas y entregas", color = Color(0xFFEBDAC8),
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                // Solo dos cifras principales para que el historial tenga mas espacio.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumenAnuncio("Anuncios", state.items.size.toString(), Modifier.weight(1f))
                    ResumenAnuncio("Por asignar", state.items.count {
                        (it.cantidadPactada - it.cantidadAsignada) > 0.000001
                    }.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Nuevo anuncio", "Historial").forEachIndexed { index, label ->
                        val seleccionado = tab == index
                        Surface(
                            onClick = { tab = index },
                            modifier = Modifier.weight(1f),
                            color = if (seleccionado) Verde else Color.Transparent,
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Text(label,
                                modifier = Modifier.padding(vertical = 9.dp),
                                color = if (seleccionado) Color.White else Cafe,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            when (tab) {
                0 -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White,
                            shadowElevation = 2.dp) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Registrar nuevo anuncio", color = Cafe,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                Text("Ingresa los datos pactados con el comprador.",
                                    color = Suave, style = MaterialTheme.typography.bodySmall)
                                OutlinedTextField(
                                    value = cliente,
                                    onValueChange = { cliente = it },
                                    label = { Text("Cliente") },
                                    modifier = Modifier.fillMaxWidth(), singleLine = true
                                )
                                OutlinedTextField(
                                    value = formatNumber(cantidadTxt),
                                    onValueChange = { cantidadTxt = it.filter(Char::isDigit) },
                                    label = { Text("Cantidad pactada (KG)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(), singleLine = true
                                )
                                OutlinedTextField(
                                    value = formatNumber(precioTxt),
                                    onValueChange = { precioTxt = it.filter(Char::isDigit) },
                                    label = { Text("Precio unitario (COP)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(), singleLine = true
                                )
                                OutlinedTextField(
                                    value = notaTxt,
                                    onValueChange = { notaTxt = it },
                                    label = { Text("Nota (opcional)") },
                                    modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3
                                )
                                Surface(color = Color(0xFFEAF2E9),
                                    shape = RoundedCornerShape(14.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Total pactado", color = Verde,
                                            style = MaterialTheme.typography.labelLarge)
                                        Text(pesos(total), color = Verde,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                Button(
                                    onClick = {
                                        guardandoAnuncio = true
                                        vm.registrar(
                                            VentaPedidoInput(
                                                cliente = cliente.trim(),
                                                cantidadPactada = cantidad,
                                                unidad = "KG",
                                                precioUnitVenta = precio,
                                                nota = notaTxt.trim().ifBlank { null }
                                            )
                                        )
                                    },
                                    enabled = !state.loading && cliente.isNotBlank() &&
                                            cantidad > 0 && precio > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(if (state.loading) "Guardando…" else "Registrar anuncio") }
                                if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
                1 -> HistorialAnuncios(
                    items = state.items,
                    loading = state.loading,
                    onIrAsignacionMezcla = onIrAsignacionMezcla
                )
            }
        }
    }
}

@Composable
private fun ResumenAnuncio(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(titulo, color = Suave, style = MaterialTheme.typography.labelMedium)
            Text(valor, color = Verde, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HistorialAnuncios(
    items: List<VentaPedido>,
    loading: Boolean,
    onIrAsignacionMezcla: (String) -> Unit
) {
    var filtro by remember { mutableStateOf(FiltroAnuncio.TODOS) }
    val filtrados = remember(items, filtro) {
        if (filtro.estado == null) items
        else items.filter { EstadoAnuncio.from(it.estado) == filtro.estado }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            FiltroAnuncio.entries.forEach { actual ->
                FilterChip(
                    selected = filtro == actual,
                    onClick = { filtro = actual },
                    label = { Text(actual.titulo, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Verde,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Cafe
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filtro == actual,
                        borderColor = Color(0xFFD5C5B5),
                        selectedBorderColor = Verde
                    )
                )
            }
        }
        if (loading && items.isEmpty()) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (filtrados.isEmpty()) {
            Surface(modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Color.White, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.History, null, tint = Verde, modifier = Modifier.size(28.dp))
                    Text("No hay anuncios para este filtro", color = Cafe,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(filtrados, key = { it.id }) { anuncio ->
                    TarjetaAnuncio(anuncio, onIrAsignacionMezcla)
                }
            }
        }
    }
}

@Composable
private fun TarjetaAnuncio(
    item: VentaPedido,
    onIrAsignacionMezcla: (String) -> Unit
) {
    val estado = EstadoAnuncio.from(item.estado)
    val asignado = item.cantidadAsignada
    val pendiente = (item.cantidadPactada - asignado).coerceAtLeast(0.0)
    // Se pueden asignar varias mezclas mientras haya un saldo pendiente.
    val permiteAsignar = pendiente > 0.000001 && estado != EstadoAnuncio.ENTREGA_TOTAL
    val progreso = if (item.cantidadPactada > 0) {
        (asignado / item.cantidadPactada).toFloat().coerceIn(0f, 1f)
    } else 0f
    val fecha = remember(item.fecha) {
        SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale("es", "CO")).format(Date(item.fecha))
    }
    val colorEstado = when (estado) {
        EstadoAnuncio.CREADO -> Color(0xFF93631B)
        EstadoAnuncio.ENTREGA_PARCIAL -> Color(0xFF1E6582)
        EstadoAnuncio.ENTREGA_TOTAL -> Verde
        else -> Cafe
    }
    val fondoEstado = when (estado) {
        EstadoAnuncio.CREADO -> Color(0xFFFFF1D9)
        EstadoAnuncio.ENTREGA_PARCIAL -> Color(0xFFE4F1F8)
        EstadoAnuncio.ENTREGA_TOTAL -> Color(0xFFE6EFE6)
        else -> Color(0xFFECE8DF)
    }
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White,
        shape = RoundedCornerShape(17.dp), shadowElevation = 1.dp) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.ShoppingCart, null, tint = Verde, modifier = Modifier.size(22.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.cliente, color = Cafe, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(fecha, color = Suave, style = MaterialTheme.typography.labelSmall)
                }
                Surface(color = fondoEstado, shape = RoundedCornerShape(50.dp)) {
                    Text(when (estado) {
                        EstadoAnuncio.CREADO -> "CREADO"
                        EstadoAnuncio.ENTREGA_PARCIAL -> "PARCIAL"
                        EstadoAnuncio.ENTREGA_TOTAL -> "ASIGNADO"
                        else -> item.estado
                    }, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = colorEstado, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pactado", color = Suave, style = MaterialTheme.typography.labelSmall)
                    Text("${kilos(item.cantidadPactada)} ${item.unidad}", color = Cafe,
                        fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Asignado", color = Suave, style = MaterialTheme.typography.labelSmall)
                    Text("${kilos(asignado)} ${item.unidad}", color = Cafe,
                        fontWeight = FontWeight.SemiBold)
                }
            }
            LinearProgressIndicator(progress = { progreso },
                modifier = Modifier.fillMaxWidth().height(5.dp), color = Verde,
                trackColor = Color(0xFFEAF2E9))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Por asignar: ${kilos(pendiente)} ${item.unidad}", color = Suave,
                    style = MaterialTheme.typography.bodySmall)
                Text(pesos(item.totalVenta), color = Verde, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium)
            }
            // Datos secundarios bajo demanda para ahorrar espacio.
            var verDetalle by remember(item.id) { mutableStateOf(false) }
            TextButton(onClick = { verDetalle = !verDetalle },
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Cafe)) {
                Text(if (verDetalle) "Ocultar detalles" else "Ver detalles",
                    style = MaterialTheme.typography.labelMedium)
            }
            if (verDetalle) {
                HorizontalDivider(color = Color(0xFFECE8DF))
                Text("Precio unitario: ${pesos(item.precioUnitVenta)}", color = Suave,
                    style = MaterialTheme.typography.bodySmall)
                if (!item.nota.isNullOrBlank()) {
                    Text("Nota: ${item.nota}", color = Suave,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            if (permiteAsignar) {
                Button(onClick = { onIrAsignacionMezcla(item.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(11.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)) {
                    Text(if (asignado > 0.0) "Completar asignación" else "Asignar mezcla")
                }
            }
        }
    }
}
