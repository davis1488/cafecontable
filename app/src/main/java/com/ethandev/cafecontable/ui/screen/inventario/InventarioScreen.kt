package com.ethandev.cafecontable.ui.screen.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Cafe = Color(0xFF493628)
private val Verde = Color(0xFF376B4B)
private val Fondo = Color(0xFFF7F5EF)
private val Secundario = Color(0xFF786D63)
private fun n(x: Double) = NumberFormat.getNumberInstance(Locale("es", "CO"))
    .apply { maximumFractionDigits = 2 }.format(x)

@Composable
fun InventarioScreen(vm: InventarioViewModel) {
    val state by vm.state.collectAsState()
    val mezclas by vm.mezclas.collectAsState()
    var pestaña by remember { mutableIntStateOf(0) }
    var filtro by remember { mutableStateOf("Todos") }
    LaunchedEffect(Unit) { vm.cargarInventario() }

    // Resumen histórico de movimientos registrados en Kardex, no de compras contables.
    val entradas = state.movimientos.filter { it.tipo == "ENTRADA" || it.tipo == "AJUSTE" }
        .sumOf { it.cantidad }
    val salidas = state.movimientos.filter { it.tipo == "SALIDA" }.sumOf { it.cantidad }
    val movimientosFiltrados = state.movimientos.filter {
        when (filtro) {
            "Entradas" -> it.tipo == "ENTRADA" || it.tipo == "AJUSTE"
            "Salidas" -> it.tipo == "SALIDA"
            else -> true
        }
    }
    val materiaPrima = state.items.sumOf { if (it.unidad.equals("KG", true)) it.existencia else 0.0 }
    val bodegaMezclas = mezclas.sumOf { it.bodega }
    val transitoMezclas = mezclas.sumOf { it.transito }
    val depositoMezclas = mezclas.sumOf { it.deposito }
    val diferencia = mezclas.sumOf { it.sinConciliar }
    val inventarioPropio = materiaPrima + bodegaMezclas + transitoMezclas + depositoMezclas + diferencia
    val nombres = state.items.associate { it.productoId to (it.nombre to it.unidad) }

    Column(Modifier.fillMaxSize().background(Fondo)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(color = Cafe, shape = RoundedCornerShape(22.dp)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Inventory2, null, tint = Color(0xFFF4D9AF),
                            modifier = Modifier.size(32.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Control de inventario", color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold)
                            Text("Materia prima, mezclas propias y depósito", color = Color(0xFFEBDAC8),
                                style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { vm.cargarInventario() }) {
                            Icon(Icons.Filled.Refresh, "Actualizar", tint = Color.White)
                        }
                    }
                }
            }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.error?.let { error -> item {
                Surface(color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)) {
                    Text(error, Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            } }
            item {
                Surface(color = Color(0xFFE7EEE7), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Inventario propio total (KG)", color = Cafe,
                            fontWeight = FontWeight.SemiBold)
                        Text("${n(inventarioPropio)} kg", color = Verde,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Text("Excluye las cantidades ya asignadas a anuncios.",
                            color = Color(0xFF29231E), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Resumen("Sin mezclar", "${n(materiaPrima)} kg", Modifier.weight(1f))
                    Resumen("Mezclas bodega", "${n(bodegaMezclas)} kg", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Resumen("En tránsito", "${n(transitoMezclas)} kg", Modifier.weight(1f))
                    Resumen("En depósito", "${n(depositoMezclas)} kg", Modifier.weight(1f))
                }
                if (diferencia > 0.001) {
                    Spacer(Modifier.height(8.dp))
                    Text("${n(diferencia)} kg por conciliar: diferencia entre cantidad preparada y entregada. No se ha eliminado del saldo propio.",
                        color = Color(0xFF9A5D22), style = MaterialTheme.typography.bodySmall)
                }
                Text("Resumen en kg: los otros productos/unidades permanecen en el detalle de existencias.",
                    color = Secundario, style = MaterialTheme.typography.labelSmall)
            }
            item {
                TabRow(selectedTabIndex = pestaña,
                    containerColor = Fondo, contentColor = Verde) {
                    Tab(selected = pestaña == 0, onClick = { pestaña = 0 },
                        text = { Text("Existencias") })
                    Tab(selected = pestaña == 1, onClick = { pestaña = 1 },
                        text = { Text("Historial") })
                }
            }
            if (pestaña == 0) {
                item {
                    Text("Mezclas propias sin anunciar", color = Cafe,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                val pendientes = mezclas.filter { it.propio > 0.001 }
                if (!state.loading && pendientes.isEmpty()) item {
                    Text("No hay saldo pendiente en mezclas.", color = Secundario)
                }
                items(pendientes, key = { "mezcla_${it.id}" }) { m ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(m.nota?.takeIf { it.isNotBlank() } ?: "Mezcla ${m.id.take(8)}",
                                color = Cafe, fontWeight = FontWeight.Bold)
                            Text("Estado: ${m.estado}", color = Color(0xFF29231E))
                            Text("Preparado: ${n(m.total)} kg · Anunciado: ${n(m.asignado)} kg",
                                color = Color(0xFF29231E))
                            Text("Saldo propio: ${n(m.propio)} kg",
                                color = Verde, fontWeight = FontWeight.Bold)
                            if (m.deposito > 0) Text("En depósito: ${n(m.deposito)} kg",
                                color = Color(0xFF9A5D22), fontWeight = FontWeight.SemiBold)
                            if (m.bodega > 0) Text("En bodega: ${n(m.bodega)} kg", color = Cafe)
                            if (m.transito > 0) Text("En tránsito: ${n(m.transito)} kg", color = Cafe)
                            if (m.sinConciliar > 0) Text("Por conciliar: ${n(m.sinConciliar)} kg",
                                color = Color(0xFF9A5D22))
                        }
                    }
                }
                item {
                    Text("Materia prima en Kardex", color = Cafe,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                if (!state.loading && state.items.isEmpty()) item {
                    Text("No hay productos registrados. Puedes consultar el historial en la otra pestaña.",
                        color = Secundario)
                }
                items(state.items, key = { "producto_${it.productoId}" }) { prod ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(prod.nombre, color = Cafe, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium)
                                Text(if (prod.existencia <= 0) "Agotado" else "Disponible",
                                    color = if (prod.existencia <= 0) Color(0xFFAB453C) else Verde,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${n(prod.existencia)} ${prod.unidad}",
                                color = Cafe, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Todos", "Entradas", "Salidas").forEach { opcion ->
                            FilterChip(selected = filtro == opcion, onClick = { filtro = opcion },
                                label = { Text(opcion) })
                        }
                    }
                }
                if (!state.loading && movimientosFiltrados.isEmpty()) item {
                    Text("No hay movimientos para este filtro.", color = Secundario)
                }
                items(movimientosFiltrados, key = { "mov_${it.id}" }) { mov ->
                    val producto = nombres[mov.productoId]
                    val entrada = mov.tipo == "ENTRADA" || mov.tipo == "AJUSTE"
                    val fecha = remember(mov.fecha) {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))
                            .format(Date(mov.fecha))
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(producto?.first ?: "Producto ${mov.productoId}",
                                        fontWeight = FontWeight.Bold, color = Cafe)
                                    Text(fecha, color = Secundario,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                Text("${if (entrada) "+" else "−"}${n(mov.cantidad)} ${producto?.second ?: ""}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (entrada) Verde else Color(0xFFB45309))
                            }
                            Text("${mov.tipo} · ${mov.docTipo ?: "Sin documento"}${mov.docId?.let { " · $it" } ?: ""}",
                                color = Secundario, style = MaterialTheme.typography.bodySmall)
                            if (!mov.nota.isNullOrBlank()) {
                                Text(mov.nota.orEmpty(), color = Secundario,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Resumen(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(10.dp)) {
            Text(titulo, color = Secundario, style = MaterialTheme.typography.labelSmall)
            Text(valor, color = Verde, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
        }
    }
}
