package com.ethandev.cafecontable.ui.screen.cierre

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.repository.CierreSemanalRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Cafe = Color(0xFF493628)
private val Verde = Color(0xFF376B4B)
private val Crema = Color(0xFFF7F5EF)
private val Texto = Color(0xFF29231E)
private val Gris = Color(0xFF665C53)
private val VerdeClaro = Color(0xFFE7EEE7)

private fun inicioDia(t: Long): Long = Calendar.getInstance().apply {
    timeInMillis = t
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun sumarDias(t: Long, dias: Int): Long = Calendar.getInstance().apply {
    timeInMillis = t
    add(Calendar.DAY_OF_MONTH, dias)
}.timeInMillis

private fun semanaActual(): Pair<Long, Long> {
    val c = Calendar.getInstance()
    val desplazamiento = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7 // lunes = 0
    val lunes = sumarDias(inicioDia(c.timeInMillis), -desplazamiento)
    return lunes to sumarDias(lunes, 7)
}
private fun mesActual(): Pair<Long, Long> {
    val c = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val inicio = inicioDia(c.timeInMillis)
    c.add(Calendar.MONTH, 1)
    return inicio to inicioDia(c.timeInMillis)
}
private fun fecha(t: Long) = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO")).format(Date(t))
private fun pesos(v: Double) = "$ " + NumberFormat.getNumberInstance(Locale("es", "CO")).apply { maximumFractionDigits = 0 }.format(v)
private fun kg(v: Double) = NumberFormat.getNumberInstance(Locale("es", "CO")).apply { maximumFractionDigits = 2 }.format(v) + " kg"

@Composable
fun CierreSemanalScreen(database: AppDatabase) {
    val vm: CierreSemanalViewModel = viewModel(factory = remember(database) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CierreSemanalViewModel(CierreSemanalRepository(database)) as T
        }
    })
    val state by vm.state.collectAsStateWithLifecycle()
    var filtro by remember { mutableIntStateOf(0) }
    var periodo by remember { mutableStateOf(semanaActual()) }
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val actualizar by rememberUpdatedState(newValue = { vm.cargar(periodo.first, periodo.second) })

    LaunchedEffect(periodo) { vm.cargar(periodo.first, periodo.second) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) actualizar()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    fun seleccionarFecha(esInicio: Boolean) {
        val base = if (esInicio) periodo.first else sumarDias(periodo.second, -1)
        val c = Calendar.getInstance().apply { timeInMillis = base }
        DatePickerDialog(ctx, { _, y, m, d ->
            val elegido = Calendar.getInstance().apply { set(y, m, d) }
            val dia = inicioDia(elegido.timeInMillis)
            val nuevo = if (esInicio) dia to periodo.second else periodo.first to sumarDias(dia, 1)
            if (nuevo.second > nuevo.first) { periodo = nuevo; filtro = 3 }
            else android.widget.Toast.makeText(ctx, "La fecha final debe ser igual o posterior a la inicial", android.widget.Toast.LENGTH_SHORT).show()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Crema),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(color = Cafe, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Análisis y cierres", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Reporte financiero por período", color = Color(0xFFF4D9AF), style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { vm.cargar(periodo.first, periodo.second) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Semana", "Anterior", "Mes", "Fechas").forEachIndexed { i, texto ->
                    Surface(
                        onClick = {
                            filtro = i
                            periodo = when (i) {
                                0 -> semanaActual()
                                1 -> semanaActual().let { sumarDias(it.first, -7) to it.first }
                                2 -> mesActual()
                                else -> periodo
                            }
                        },
                        color = if (filtro == i) Verde else Color.White,
                        shape = RoundedCornerShape(9.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(texto, Modifier.padding(vertical = 11.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (filtro == i) Color.White else Cafe, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { seleccionarFecha(true) }, modifier = Modifier.weight(1f)) { Text("Desde: ${fecha(periodo.first)}", maxLines = 1) }
                OutlinedButton(onClick = { seleccionarFecha(false) }, modifier = Modifier.weight(1f)) { Text("Hasta: ${fecha(sumarDias(periodo.second, -1))}", maxLines = 1) }
            }
        }
        if (state.cargando) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = Verde) }
        state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
        state.resumen?.let { r ->
            item { Text("Compras realizadas", color = Cafe, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Indicador("Café comprado", kg(r.comprasKg), Modifier.weight(1f))
                Indicador("Promedio/kg", pesos(r.precioPromedioCompra), Modifier.weight(1f))
            } }
            item { ResumenFila("${r.comprasCantidad} compras registradas", pesos(r.comprasValor)) }
            item { Text("Ventas liquidadas", color = Cafe, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Indicador("Kilos liquidados", kg(r.ventasKg), Modifier.weight(1f))
                Indicador("Promedio neto/kg", pesos(r.precioPromedioVenta), Modifier.weight(1f))
            } }
            item { ResumenFila("${r.ventasCantidad} liquidaciones", pesos(r.ventasNeto)) }
            item {
                Surface(color = Color.White, shape = RoundedCornerShape(14.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text(
                            "Desglose de liquidaciones",
                            color = Cafe,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (r.ventasBrutas != null && r.ajustesFactor != null &&
                            r.descuentosCooperativa != null && r.otrosDescuentos != null
                        ) {
                            ResumenLinea("Ventas brutas", pesos(r.ventasBrutas))
                            ResumenLinea("Ajustes por factor", pesos(r.ajustesFactor))
                            ResumenLinea("Descuentos cooperativa", "− " + pesos(r.descuentosCooperativa))
                            ResumenLinea("Otros descuentos", "− " + pesos(r.otrosDescuentos))
                            HorizontalDivider()
                            ResumenLinea("Ventas netas", pesos(r.ventasNeto))
                            if (!r.desgloseVentasCompleto) {
                                Text(
                                    "Atención: el desglose no coincide con la venta neta.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Text(
                                "Desglose no disponible para la estructura actual de liquidaciones.",
                                color = Gris,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "Los descuentos ya están incluidos en la venta neta; no se restan otra vez de la utilidad.",
                            color = Gris,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            item { Text("Gastos y rentabilidad", color = Cafe, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
            item { Surface(color = Color.White, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    ResumenLinea("Gastos registrados en el período", r.gastosRegistrados?.let(::pesos) ?: "Por verificar")
                    ResumenLinea("Costo del café liquidado", r.costoCafeLiquidado?.let(::pesos) ?: "Pendiente de conciliar")
                    ResumenLinea("Gastos imputados a liquidaciones", r.gastosUtilidad?.let(::pesos) ?: "Pendiente de conciliar")
                    HorizontalDivider()
                    Text("Los gastos registrados por fecha pueden corresponder a otras ventas. No se descuentan dos veces.", color = Gris, style = MaterialTheme.typography.bodySmall)
                }
            } }
            item { Surface(color = VerdeClaro, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Utilidad neta de liquidaciones conciliadas", color = Cafe, fontWeight = FontWeight.SemiBold)
                    Text(r.utilidadNeta?.let(::pesos) ?: "Pendiente de conciliar", color = Verde, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Text(if (r.utilidadCompleta) "Conciliación completa" else "No se mostrará una utilidad aproximada sin validar costos y gastos", color = Gris, style = MaterialTheme.typography.bodySmall)
                }
            } }
            if (r.avisos.isNotEmpty()) item { Surface(color = Color.White, shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Observaciones del reporte", color = Cafe, fontWeight = FontWeight.Bold)
                    r.avisos.forEach { Text("• $it", color = Gris, style = MaterialTheme.typography.bodySmall) }
                }
            } }
        }
        item { Text("Reporte dinámico: no bloquea operaciones ni guarda un cierre definitivo.", color = Gris, style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun Indicador(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(titulo, color = Gris, style = MaterialTheme.typography.labelSmall)
            Text(valor, color = Verde, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}
@Composable
private fun ResumenFila(titulo: String, valor: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(13.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(titulo, color = Gris, style = MaterialTheme.typography.bodySmall)
            Text(valor, color = Cafe, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
private fun ResumenLinea(titulo: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titulo, color = Gris, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(valor, color = Texto, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
    }
}
