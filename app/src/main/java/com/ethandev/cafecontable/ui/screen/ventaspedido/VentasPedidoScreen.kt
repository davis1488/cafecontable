package com.ethandev.cafecontable.ui.screen.ventaspedido

import androidx.compose.foundation.background
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

    LaunchedEffect(Unit) { vm.cargarVentas() }

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
                    snackbarData  = data,
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
            Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Cafe
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(color = Color(0xFF6B5240), shape = RoundedCornerShape(13.dp)) {
                            Icon(
                                Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = Dorado,
                                modifier = Modifier.padding(11.dp).size(26.dp)
                            )
                        }
                        Column {
                            Text("Anuncios de venta", color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold)
                            Text("Pedidos, mezclas y entregas", color = Color(0xFFEBDAC8),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumenAnuncio("Total", state.items.size.toString(), Modifier.weight(1f))
                    ResumenAnuncio("Pendientes", state.items.count {
                        EstadoAnuncio.from(it.estado) == EstadoAnuncio.CREADO ||
                                EstadoAnuncio.from(it.estado) == EstadoAnuncio.ENTREGA_PARCIAL
                    }.toString(), Modifier.weight(1f))
                    ResumenAnuncio("Entregados", state.items.count {
                        EstadoAnuncio.from(it.estado) == EstadoAnuncio.ENTREGA_TOTAL
                    }.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = Color.White,
                    contentColor = Verde,
                    divider = {}
                ) {
                    Tab(selected = tab == 0, onClick = { tab = 0 },
                        text = { Text("Nuevo anuncio", fontWeight = FontWeight.SemiBold) })
                    Tab(selected = tab == 1, onClick = { tab = 1 },
                        text = { Text("Historial", fontWeight = FontWeight.SemiBold) })
                }
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
    Surface(modifier = modifier, color = Color.White,
        shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, color = Verde, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(titulo, color = Suave, style = MaterialTheme.typography.labelSmall)
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
        ScrollableTabRow(
            selectedTabIndex = filtro.ordinal,
            edgePadding = 16.dp,
            containerColor = Fondo,
            contentColor = Verde,
            divider = {}
        ) {
            FiltroAnuncio.entries.forEach { actual ->
                Tab(
                    selected = filtro == actual,
                    onClick = { filtro = actual },
                    text = { Text(actual.titulo) }
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Historial de anuncios", color = Cafe,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            Text("${filtrados.size} registros", color = Verde,
                style = MaterialTheme.typography.labelMedium)
        }
        if (loading && items.isEmpty()) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (filtrados.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.History, contentDescription = null,
                        tint = Verde, modifier = Modifier.size(32.dp))
                    Text("No hay anuncios para este filtro", color = Cafe,
                        fontWeight = FontWeight.SemiBold)
                    Text("Prueba otro estado o registra un anuncio nuevo.",
                        color = Suave, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val entregado = item.cantidadAsignada
    val pendiente = (item.cantidadPactada - entregado).coerceAtLeast(0.0)
    val progreso = if (item.cantidadPactada > 0) {
        (entregado / item.cantidadPactada).toFloat().coerceIn(0f, 1f)
    } else 0f
    val fecha = remember(item.fecha) {
        SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()).format(Date(item.fecha))
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = Color(0xFFE6EFE6), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null,
                        tint = Verde, modifier = Modifier.padding(10.dp).size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(item.cliente, color = Cafe, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(fecha, color = Suave,
                        style = MaterialTheme.typography.bodySmall)
                }
                Surface(color = fondoEstado, shape = RoundedCornerShape(50.dp)) {
                    Text(
                        when (estado) {
                            EstadoAnuncio.CREADO -> "CREADO"
                            EstadoAnuncio.ENTREGA_PARCIAL -> "PARCIAL"
                            EstadoAnuncio.ENTREGA_TOTAL -> "ENTREGADO"
                            else -> item.estado
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = colorEstado,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFECE8DF))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pactado", color = Suave, style = MaterialTheme.typography.labelMedium)
                    Text("${kilos(item.cantidadPactada)} ${item.unidad}", color = Cafe,
                        fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Asignado a mezclas", color = Suave,
                        style = MaterialTheme.typography.labelMedium)
                    Text("${kilos(entregado)} ${item.unidad}", color = Cafe,
                        fontWeight = FontWeight.SemiBold)
                }
            }
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth().height(7.dp),
                color = Verde,
                trackColor = Color(0xFFEAF2E9)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pendiente por asignar", color = Suave,
                    style = MaterialTheme.typography.bodySmall)
                Text("${kilos(pendiente)} ${item.unidad}", color = Verde,
                    fontWeight = FontWeight.Bold)
            }
            Surface(color = Color(0xFFEAF2E9), shape = RoundedCornerShape(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total pactado", color = Verde,
                        style = MaterialTheme.typography.labelMedium)
                    Text(pesos(item.totalVenta), color = Verde, fontWeight = FontWeight.Bold)
                }
            }
            Text("Precio unitario: ${pesos(item.precioUnitVenta)}", color = Suave,
                style = MaterialTheme.typography.bodySmall)
            if (!item.nota.isNullOrBlank()) {
                Text("Nota: ${item.nota}", color = Suave,
                    style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { onIrAsignacionMezcla(item.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Verde),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Asignar mezcla")
            }
        }
    }
}
