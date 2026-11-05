package com.ethandev.cafecontable.ui.screen.liquidacion

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ethandev.cafecontable.domain.model.LiquidacionHistorial
import com.ethandev.cafecontable.domain.model.LiquidacionPendiente
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

private val Cafe = Color(0xFF493628)
private val Verde = Color(0xFF376B4B)
private val Crema = Color(0xFFF7F5EF)
private val Texto = Color(0xFF29231E)
private val Secundario = Color(0xFF665C53)
private val Borde = Color(0xFFD5C5B5)
private val Rojo = Color(0xFFC62828)
private val Dorado = Color(0xFFF4D9AF)
private val VerdeClaro = Color(0xFFE7EEE7)
private val Forma = RoundedCornerShape(18.dp)

@Composable
fun LiquidacionScreen(viewModel: LiquidacionViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pestaña by remember { mutableIntStateOf(0) }

    // El lifecycleOwner puede ser el NavBackStackEntry proporcionado por Navigation Compose.
    // Se actualiza al entrar/regresar a Liquidaciones, sin reiniciar la app.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarTodo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Mensajes de éxito/error tras guardar una liquidación.
    LaunchedEffect(state.okMsg, state.error) {
        val mensaje = state.error ?: state.okMsg
        if (mensaje != null) {
            val esError = state.error != null
            viewModel.limpiarMensajes()
            snackbarHostState.showSnackbar(
                message = mensaje,
                withDismissAction = esError,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        containerColor = Crema,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Cafe,
                    contentColor = Color.White,
                    actionColor = Dorado,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp)) {
                Surface(color = Cafe, shape = Forma, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Liquidaciones", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Control de entregas, ajustes y pagos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Dorado,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.cargarTodo() },
                                enabled = !state.loading
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Actualizar liquidaciones",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ResumenCantidad("Pendientes", state.pendientes.size.toString(), Modifier.weight(1f))
                    ResumenCantidad("Liquidadas", state.historial.size.toString(), Modifier.weight(1f))
                    ResumenCantidad("Total", (state.pendientes.size + state.historial.size).toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    listOf("Pendientes", "Historial").forEachIndexed { index, nombre ->
                        val activo = pestaña == index
                        Surface(
                            onClick = { pestaña = index },
                            modifier = Modifier.weight(1f),
                            color = if (activo) Verde else Color.Transparent,
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Text(nombre, modifier = Modifier.padding(vertical = 12.dp),
                                color = if (activo) Color.White else Cafe,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
            if (state.loading && state.pendientes.isEmpty() && state.historial.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Verde)
                }
            } else if (pestaña == 0) {
                PendientesLiquidacionTab(
                    pendientes = state.pendientes,
                    guardando = state.loading,
                    onGuardar = { item, cooperativa, otros, nota ->
                        viewModel.registrarLiquidacion(item, cooperativa, otros, nota)
                    }
                )
            } else {
                HistorialLiquidacionTab(state.historial)
            }
        }
    }
}

@Composable
private fun ResumenCantidad(titulo: String, numero: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(numero, color = Verde, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge)
            Text(titulo, color = Secundario, style = MaterialTheme.typography.labelSmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PendientesLiquidacionTab(
    pendientes: List<LiquidacionPendiente>,
    guardando: Boolean,
    onGuardar: (LiquidacionPendiente, Long, Long, String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Asignaciones por liquidar (${pendientes.size})", fontWeight = FontWeight.Bold,
                color = Cafe, style = MaterialTheme.typography.titleMedium)
        }
        if (pendientes.isEmpty()) {
            item { EstadoVacio("No hay asignaciones pendientes por liquidar.") }
        }
        items(pendientes, key = { it.asignacionId }) { pendiente ->
            PendienteLiquidacionCard(pendiente, guardando, onGuardar)
        }
    }
}

@Composable
private fun PendienteLiquidacionCard(
    item: LiquidacionPendiente,
    guardando: Boolean,
    onGuardar: (LiquidacionPendiente, Long, Long, String?) -> Unit
) {
    var expandido by remember(item.asignacionId) { mutableStateOf(false) }
    var porcentajeTxt by remember(item.asignacionId) { mutableStateOf("0") }
    var otrosTxt by remember(item.asignacionId) { mutableStateOf("0") }
    var nota by remember(item.asignacionId) { mutableStateOf("") }

    // Mismas fórmulas del módulo original; base del factor = 90.
    val porcentaje = porcentajeTxt.toDoubleOrNull() ?: 0.0
    val otros = otrosTxt.toLongOrNull() ?: 0L
    val valorBase = (item.cantidadKg * item.precioBaseKg.toDouble()).roundToLong()
    val ajusteFactor = (valorBase * (90.0 - item.factorReal) / 100.0).roundToLong()
    val valorAjustado = valorBase + ajusteFactor
    val descuentoCooperativa = (valorAjustado * porcentaje / 100.0).roundToLong()
    val neto = valorAjustado - descuentoCooperativa - otros
    val datosValidos = porcentaje.isFinite() && porcentaje in 0.0..100.0 &&
            neto >= 0L && item.cantidadKg > 0.0 && item.precioBaseKg > 0L

    Surface(shape = Forma, color = Color.White, modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(item.cliente?.takeIf { it.isNotBlank() } ?: "Sin cliente",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cafe)
                    Text("Asignación ${item.asignacionId.takeLast(8)}", color = Secundario,
                        style = MaterialTheme.typography.labelSmall)
                }
                EstadoEtiqueta("PENDIENTE", Color(0xFFFFF2DD), Cafe)
            }
            Dato("Cantidad entregada", "${formatoKg(item.cantidadKg)} kg")
            Dato("Precio base/kg", formatoPeso(item.precioBaseKg))
            Dato("Factor real", formatoKg(item.factorReal))
            Dato("Pedido", item.pedidoId)
            Dato("Mezcla", item.mezclaId)

            if (!expandido) {
                Button(
                    onClick = { expandido = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Calcular y liquidar", color = Color.White) }
            } else {
                HorizontalDivider(color = Borde)
                Text("Detalle de liquidación", fontWeight = FontWeight.Bold, color = Cafe)
                Dato("Valor base", formatoPeso(valorBase))
                Dato(
                    when {
                        ajusteFactor < 0 -> "Descuento por factor"
                        ajusteFactor > 0 -> "Bonificación por factor"
                        else -> "Ajuste por factor"
                    },
                    formatoPeso(kotlin.math.abs(ajusteFactor))
                )
                Dato("Valor ajustado", formatoPeso(valorAjustado))
                OutlinedTextField(
                    value = porcentajeTxt,
                    onValueChange = { entrada ->
                        val valor = entrada.replace(',', '.')
                        if (valor.matches(Regex("""\d*(\.\d*)?"""))) porcentajeTxt = valor
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descuento cooperativa (%)") },
                    supportingText = { Text("Equivale a ${formatoPeso(descuentoCooperativa)}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = coloresCampo()
                )
                OutlinedTextField(
                    value = otrosTxt,
                    onValueChange = { otrosTxt = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Otros descuentos (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = coloresCampo()
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota (opcional)") },
                    minLines = 2,
                    colors = coloresCampo()
                )
                Surface(color = VerdeClaro, shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("Neto a pagar", color = Cafe, style = MaterialTheme.typography.labelLarge)
                        Text(formatoPeso(neto), color = Verde, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall)
                    }
                }
                if (!datosValidos) {
                    Text("Revisa los descuentos: el porcentaje debe estar entre 0 y 100 y el neto no puede ser negativo.",
                        color = Rojo, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { onGuardar(item, descuentoCooperativa, otros, nota.ifBlank { null }) },
                    enabled = datosValidos && !guardando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Guardar liquidación")
                }
                OutlinedButton(
                    onClick = { expandido = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Cafe),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Ocultar detalle") }
            }
        }
    }
}

@Composable
private fun HistorialLiquidacionTab(historial: List<LiquidacionHistorial>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Historial de liquidaciones (${historial.size})", fontWeight = FontWeight.Bold,
                color = Cafe, style = MaterialTheme.typography.titleMedium)
        }
        if (historial.isEmpty()) item { EstadoVacio("No hay liquidaciones registradas.") }
        items(historial, key = { it.id }) { item ->
            HistorialLiquidacionCard(item)
        }
    }
}

@Composable
private fun HistorialLiquidacionCard(item: LiquidacionHistorial) {
    var detalles by remember(item.id) { mutableStateOf(false) }
    Surface(shape = Forma, color = Color.White, shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(item.cliente?.takeIf { it.isNotBlank() } ?: "Sin cliente",
                        color = Cafe, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text(formatoFecha(item.fecha), color = Secundario,
                        style = MaterialTheme.typography.bodySmall)
                }
                EstadoEtiqueta("LIQUIDADA", VerdeClaro, Verde)
            }
            Dato("Cantidad", "${formatoKg(item.cantidadKg)} kg")
            Dato("Factor", formatoKg(item.factorReal))
            Surface(color = VerdeClaro, shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Valor neto", color = Cafe, fontWeight = FontWeight.SemiBold)
                    Text(formatoPeso(item.valorNeto), color = Verde,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
            TextButton(onClick = { detalles = !detalles },
                colors = ButtonDefaults.textButtonColors(contentColor = Cafe)) {
                Text(if (detalles) "Ocultar detalles" else "Ver detalles")
            }
            if (detalles) {
                HorizontalDivider(color = Borde)
                Dato("Pedido", item.pedidoId)
                Dato("Mezcla", item.mezclaId)
                Dato("Asignación", item.asignacionId)
                Dato("Precio base/kg", formatoPeso(item.precioBaseKg))
                Dato("Valor base", formatoPeso(item.valorBase))
                Dato("Ajuste por factor", formatoPeso(item.ajusteFactor))
                Dato("Descuento cooperativa", formatoPeso(item.descuentoCooperativa))
                Dato("Otros descuentos", formatoPeso(item.otrosDescuentos))
                if (!item.nota.isNullOrBlank()) Dato("Nota", item.nota)
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(etiqueta, color = Secundario, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.45f))
        Text(valor, color = Texto, fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.55f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun EstadoEtiqueta(texto: String, fondo: Color, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = fondo) {
        Text(texto, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EstadoVacio(texto: String) {
    Surface(shape = Forma, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(texto, color = Secundario, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Texto,
    unfocusedTextColor = Texto,
    focusedLabelColor = Verde,
    unfocusedLabelColor = Secundario,
    focusedBorderColor = Verde,
    unfocusedBorderColor = Borde,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

private fun formatoPeso(valor: Long): String =
    "$ " + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(valor)

private fun formatoKg(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply { maximumFractionDigits = 2 }.format(valor)

private fun formatoFecha(fecha: Long): String =
    DateFormat.format("dd/MM/yyyy HH:mm", fecha).toString()
