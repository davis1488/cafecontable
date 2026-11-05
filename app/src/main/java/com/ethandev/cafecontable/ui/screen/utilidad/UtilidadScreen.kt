package com.ethandev.cafecontable.ui.screen.utilidad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.model.UtilidadOperacionModel
import com.ethandev.cafecontable.domain.model.UtilidadPendienteModel
import com.ethandev.cafecontable.domain.repository.ResumenUtilidadModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

private val CafeUtilidad = Color(0xFF493628)
private val VerdeUtilidad = Color(0xFF376B4B)
private val FondoUtilidad = Color(0xFFF7F5EF)
private val TextoUtilidad = Color(0xFF29231E)
private val SecundarioUtilidad = Color(0xFF665C53)
private val VerdeClaroUtilidad = Color(0xFFE7EEE7)
private val RojoUtilidad = Color(0xFFC62828)
private val BordeUtilidad = Color(0xFFD5C5B5)

private fun dineroUtilidad(valor: Long): String =
    "$ " + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(valor)

private fun numeroUtilidad(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 2
    }.format(valor)

private fun porcentajeUtilidad(valor: Double): String = "${numeroUtilidad(valor)} %"

@Composable
fun UtilidadScreen(viewModel: UtilidadViewModel) {
    val state by viewModel.state.collectAsState()
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarTodo() }
    LaunchedEffect(state.mensaje, state.error) {
        val mensaje = state.error ?: state.mensaje
        if (!mensaje.isNullOrBlank()) {
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Scaffold(
        containerColor = FondoUtilidad,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (state.error != null) RojoUtilidad else CafeUtilidad,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(FondoUtilidad)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(color = CafeUtilidad, shape = RoundedCornerShape(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFFF4D9AF),
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Utilidades",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Rentabilidad de compras, mezclas y entregas",
                                    color = Color(0xFFEBDAC8),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { viewModel.cargarTodo() }) {
                                Icon(Icons.Filled.Refresh, "Actualizar", tint = Color.White)
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResumenContadorUtilidad(
                            "Pendientes", state.pendientes.size.toString(), Modifier.weight(1f)
                        )
                        ResumenContadorUtilidad(
                            "Calculadas", state.historial.size.toString(), Modifier.weight(1f)
                        )
                        ResumenContadorUtilidad(
                            "Total", (state.pendientes.size + state.historial.size).toString(), Modifier.weight(1f)
                        )
                    }
                }
                item {
                    TabRow(
                        selectedTabIndex = tabSeleccionado,
                        containerColor = FondoUtilidad,
                        contentColor = VerdeUtilidad
                    ) {
                        listOf("Pendientes", "Historial", "Resumen").forEachIndexed { index, titulo ->
                            Tab(
                                selected = tabSeleccionado == index,
                                onClick = { tabSeleccionado = index },
                                selectedContentColor = VerdeUtilidad,
                                unselectedContentColor = SecundarioUtilidad,
                                text = { Text(titulo, maxLines = 1, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
                if (state.cargando) {
                    item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = VerdeUtilidad) }
                }
                when (tabSeleccionado) {
                    0 -> {
                        if (state.pendientes.isEmpty()) {
                            item { EstadoVacioUtilidad("No hay utilidades pendientes por calcular.") }
                        } else {
                            items(state.pendientes) { pendiente ->
                                PendienteUtilidadCardModern(pendiente, state.cargando) { nota ->
                                    viewModel.calcularUtilidad(pendiente, nota)
                                }
                            }
                        }
                    }
                    1 -> {
                        if (state.historial.isEmpty()) {
                            item { EstadoVacioUtilidad("Todavía no hay utilidades registradas.") }
                        } else {
                            items(state.historial) { item -> HistorialUtilidadCardModern(item) }
                        }
                    }
                    2 -> {
                        item { ResumenUtilidadCardModern(state.resumen) }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun ResumenContadorUtilidad(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, color = VerdeUtilidad, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text(titulo, color = SecundarioUtilidad, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EstadoVacioUtilidad(mensaje: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.Assessment, contentDescription = null, tint = VerdeUtilidad,
                modifier = Modifier.size(32.dp))
            Text(mensaje, color = SecundarioUtilidad, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PendienteUtilidadCardModern(
    item: UtilidadPendienteModel,
    cargando: Boolean,
    onCalcular: (String?) -> Unit
) {
    var nota by remember { mutableStateOf("") }
    var mostrarDetalle by remember { mutableStateOf(false) }
    val costoCafe = (item.cantidadKg * item.costoPromedioKg).roundToLong()
    val utilidadBruta = item.valorVentaNeto - costoCafe
    val utilidadNeta = utilidadBruta - item.totalGastos

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(item.cliente ?: "Cliente sin nombre", modifier = Modifier.weight(1f),
                    color = CafeUtilidad, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Surface(color = Color(0xFFFFF3DF), shape = RoundedCornerShape(24.dp)) {
                    Text("PENDIENTE", color = CafeUtilidad,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            FilaUtilidad("Cantidad", "${numeroUtilidad(item.cantidadKg)} kg")
            FilaUtilidad("Venta neta", dineroUtilidad(item.valorVentaNeto))
            FilaUtilidad("Costo del café", dineroUtilidad(costoCafe))
            FilaUtilidad("Gastos asociados", dineroUtilidad(item.totalGastos))
            HorizontalDivider(color = BordeUtilidad)
            Surface(color = VerdeClaroUtilidad, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("Utilidad neta estimada", color = CafeUtilidad,
                        style = MaterialTheme.typography.labelLarge)
                    Text(dineroUtilidad(utilidadNeta),
                        color = if (utilidadNeta >= 0) VerdeUtilidad else RojoUtilidad,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
            }
            TextButton(onClick = { mostrarDetalle = !mostrarDetalle }) {
                Text(if (mostrarDetalle) "Ocultar desglose" else "Ver desglose de gastos",
                    color = VerdeUtilidad)
            }
            if (mostrarDetalle) {
                Surface(color = FondoUtilidad, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilaUtilidad("Utilidad bruta", dineroUtilidad(utilidadBruta))
                        FilaUtilidad("Gastos compra", dineroUtilidad(item.gastosCompra))
                        FilaUtilidad("Gastos mezcla", dineroUtilidad(item.gastosMezcla))
                        FilaUtilidad("Gastos entrega", dineroUtilidad(item.gastosEntrega))
                        FilaUtilidad("Total gastos", dineroUtilidad(item.totalGastos))
                    }
                }
            }
            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = camposUtilidad()
            )
            Button(
                onClick = { onCalcular(nota.trim().ifBlank { null }) },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeUtilidad)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Calcular utilidad", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun HistorialUtilidadCardModern(item: UtilidadOperacionModel) {
    var expandido by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(item.cliente ?: "Cliente sin nombre", modifier = Modifier.weight(1f),
                    color = CafeUtilidad, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Surface(color = VerdeClaroUtilidad, shape = RoundedCornerShape(24.dp)) {
                    Text("CALCULADA", color = VerdeUtilidad,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            FilaUtilidad("Venta neta", dineroUtilidad(item.valorVentaNeto))
            FilaUtilidad("Costo café", dineroUtilidad(item.costoCafe))
            FilaUtilidad("Gastos", dineroUtilidad(item.totalGastos))
            HorizontalDivider(color = BordeUtilidad)
            Surface(color = VerdeClaroUtilidad, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("Utilidad neta", color = CafeUtilidad,
                        style = MaterialTheme.typography.labelLarge)
                    Text(dineroUtilidad(item.utilidadNeta),
                        color = if (item.utilidadNeta >= 0L) VerdeUtilidad else RojoUtilidad,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Text("Margen: ${porcentajeUtilidad(item.margenPorcentaje)}",
                        color = SecundarioUtilidad, style = MaterialTheme.typography.bodyMedium)
                }
            }
            TextButton(onClick = { expandido = !expandido }) {
                Text(if (expandido) "Ocultar detalles" else "Ver detalles", color = VerdeUtilidad)
            }
            if (expandido) {
                FilaUtilidad("Utilidad bruta", dineroUtilidad(item.utilidadBruta))
                if (!item.nota.isNullOrBlank()) {
                    Text("Nota: ${item.nota}", color = SecundarioUtilidad)
                }
            }
        }
    }
}

@Composable
private fun ResumenUtilidadCardModern(resumen: ResumenUtilidadModel?) {
    if (resumen == null) {
        EstadoVacioUtilidad("No hay resumen disponible todavía.")
        return
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Resumen general", color = CafeUtilidad,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FilaUtilidad("Ventas netas", dineroUtilidad(resumen.ventasNetas))
            FilaUtilidad("Costo del café", dineroUtilidad(resumen.costoCafe))
            FilaUtilidad("Gastos", dineroUtilidad(resumen.gastos))
            HorizontalDivider(color = BordeUtilidad)
            Surface(color = VerdeClaroUtilidad, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Utilidad neta total", color = CafeUtilidad,
                        style = MaterialTheme.typography.titleSmall)
                    Text(dineroUtilidad(resumen.utilidadNeta),
                        color = if (resumen.utilidadNeta >= 0L) VerdeUtilidad else RojoUtilidad,
                        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Margen: ${porcentajeUtilidad(resumen.margenPorcentaje)}",
                        color = SecundarioUtilidad)
                }
            }
        }
    }
}

@Composable
private fun FilaUtilidad(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(etiqueta, color = SecundarioUtilidad, modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(valor, color = TextoUtilidad, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun camposUtilidad() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedLabelColor = CafeUtilidad,
    unfocusedLabelColor = TextoUtilidad,
    focusedBorderColor = VerdeUtilidad,
    unfocusedBorderColor = BordeUtilidad,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = VerdeUtilidad
)
