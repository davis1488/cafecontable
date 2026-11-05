package com.ethandev.cafecontable.ui.screen.operacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ethandev.cafecontable.core.utils.FechaUtils
import com.ethandev.cafecontable.domain.constants.EstadoOperacion
import com.ethandev.cafecontable.domain.constants.TipoOperacion
import java.text.NumberFormat
import java.util.Locale

private val CafeOperacion = Color(0xFF493628)
private val VerdeOperacion = Color(0xFF376B4B)
private val FondoOperacion = Color(0xFFF7F5EF)
private val TextoOperacion = Color(0xFF29231E)
private val SubtextoOperacion = Color(0xFF665C53)
private val BordeOperacion = Color(0xFF9B8D80)
private val VerdeClaroOperacion = Color(0xFFE7EEE7)
private val RojoOperacion = Color(0xFFC62828)

private fun monedaOperacion(valor: Long): String =
    "$ " + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(valor)

private fun nombreLegibleOperacion(nombre: String): String =
    FechaUtils.formatearNombreOperacion(nombre)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperacionScreen(viewModel: OperacionViewModel) {
    val operaciones by viewModel.operaciones.collectAsStateWithLifecycle()
    val tipoSeleccionado by viewModel.tipoSeleccionado.collectAsStateWithLifecycle()
    val gastosOperacionSeleccionada by viewModel.gastosOperacionSeleccionada.collectAsStateWithLifecycle()

    val activas = operaciones.filter { it.estado == EstadoOperacion.ACTIVA }
    val cerradas = operaciones.filter { it.estado == EstadoOperacion.CERRADA }
    val gastoAcumulado = operaciones.sumOf { it.totalGastos }

    val categorias = remember {
        listOf("Transporte", "Cargue", "Descargue", "Empaque", "Costales", "Mano de obra",
            "Secado", "Análisis de café", "Comisión", "Alimentación", "Combustible", "Peaje", "Otro")
    }
    val tipos = remember { listOf(TipoOperacion.COMPRA, TipoOperacion.MEZCLA, TipoOperacion.DESPACHO) }
    val tabs = remember { listOf("Crear", "Gastos", "Activas", "Cerradas") }
    var tabIndex by remember { mutableIntStateOf(0) }

    var expandirTipo by remember { mutableStateOf(false) }
    var expandirOperacion by remember { mutableStateOf(false) }
    var expandirCategoria by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var operacionSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var categoria by remember { mutableStateOf("Transporte") }
    var valorTxt by remember { mutableStateOf("") }
    var descripcionGasto by remember { mutableStateOf("") }
    var tercero by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }

    var mostrarDetalle by remember { mutableStateOf(false) }
    var tituloDetalle by remember { mutableStateOf("") }
    var operacionParaCerrar by remember { mutableStateOf<OperacionResumenUi?>(null) }

    val operacionSeleccionada = activas.firstOrNull { it.id == operacionSeleccionadaId }
    val valorGasto = valorTxt.toLongOrNull() ?: 0L

    LaunchedEffect(Unit) { viewModel.cargarTodo() }
    LaunchedEffect(activas.map { it.id }) {
        if (activas.none { it.id == operacionSeleccionadaId }) {
            operacionSeleccionadaId = activas.firstOrNull()?.id
        }
    }

    Column(Modifier.fillMaxSize().background(FondoOperacion)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(color = CafeOperacion, shape = RoundedCornerShape(22.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, null, tint = Color(0xFFF4D9AF),
                            modifier = Modifier.size(34.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Operaciones y gastos", color = Color.White,
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Control de costos por operación", color = Color(0xFFEBDAC8),
                                style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.cargarTodo() }) {
                            Icon(Icons.Filled.Refresh, "Actualizar", tint = Color.White)
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumenOperacion("Activas", activas.size.toString(), Modifier.weight(1f))
                    ResumenOperacion("Cerradas", cerradas.size.toString(), Modifier.weight(1f))
                    ResumenOperacion("Gastos", monedaOperacion(gastoAcumulado), Modifier.weight(1f))
                }
            }
            item {
                ScrollableTabRow(
                    selectedTabIndex = tabIndex,
                    edgePadding = 0.dp,
                    containerColor = FondoOperacion,
                    contentColor = VerdeOperacion
                ) {
                    tabs.forEachIndexed { index, titulo ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = {
                                Text(titulo, maxLines = 1,
                                    fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Medium)
                            },
                            selectedContentColor = VerdeOperacion,
                            unselectedContentColor = CafeOperacion
                        )
                    }
                }
            }

            when (tabIndex) {
                0 -> {
                    item {
                        TarjetaOperacion {
                            TituloSeccionOperacion("Crear operación", "Organiza tus compras, mezclas y despachos")
                            ExposedDropdownMenuBox(
                                expanded = expandirTipo,
                                onExpandedChange = { expandirTipo = !expandirTipo }
                            ) {
                                OutlinedTextField(
                                    value = tipoSeleccionado,
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text("Tipo de operación") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirTipo) },
                                    colors = coloresCampoOperacion(),
                                    modifier = Modifier.menuAnchor(
                                        type = MenuAnchorType.PrimaryNotEditable, enabled = true
                                    ).fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandirTipo,
                                    onDismissRequest = { expandirTipo = false }
                                ) {
                                    tipos.forEach { tipo ->
                                        DropdownMenuItem(
                                            text = { Text(tipo, color = TextoOperacion) },
                                            onClick = { viewModel.seleccionarTipo(tipo); expandirTipo = false }
                                        )
                                    }
                                }
                            }
                            CampoOperacion(nombre, { nombre = it }, "Nombre de la operación", singleLine = true)
                            CampoOperacion(descripcion, { descripcion = it }, "Descripción (opcional)")
                            Button(
                                onClick = {
                                    viewModel.crearOperacion(nombre.trim(), descripcion.trim().ifBlank { null })
                                    nombre = ""
                                    descripcion = ""
                                },
                                enabled = nombre.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeOperacion, contentColor = Color.White)
                            ) {
                                Icon(Icons.Filled.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Guardar operación", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                1 -> {
                    item {
                        TarjetaOperacion {
                            TituloSeccionOperacion("Registrar gasto", "Asocia cada gasto a una operación activa")
                            if (activas.isEmpty()) {
                                AvisoOperacion("No hay operaciones activas para registrar gastos.")
                            } else {
                                ExposedDropdownMenuBox(
                                    expanded = expandirOperacion,
                                    onExpandedChange = { expandirOperacion = !expandirOperacion }
                                ) {
                                    OutlinedTextField(
                                        value = operacionSeleccionada?.let { nombreLegibleOperacion(it.nombre) } ?: "",
                                        onValueChange = {}, readOnly = true, singleLine = true,
                                        label = { Text("Operación activa") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirOperacion) },
                                        colors = coloresCampoOperacion(),
                                        modifier = Modifier.menuAnchor(
                                            type = MenuAnchorType.PrimaryNotEditable, enabled = true
                                        ).fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandirOperacion,
                                        onDismissRequest = { expandirOperacion = false }
                                    ) {
                                        activas.forEach { op ->
                                            DropdownMenuItem(
                                                text = { Text(nombreLegibleOperacion(op.nombre), color = TextoOperacion) },
                                                onClick = { operacionSeleccionadaId = op.id; expandirOperacion = false }
                                            )
                                        }
                                    }
                                }
                            }
                            ExposedDropdownMenuBox(
                                expanded = expandirCategoria,
                                onExpandedChange = { expandirCategoria = !expandirCategoria }
                            ) {
                                OutlinedTextField(
                                    value = categoria, onValueChange = {}, readOnly = true,
                                    label = { Text("Categoría del gasto") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirCategoria) },
                                    colors = coloresCampoOperacion(),
                                    modifier = Modifier.menuAnchor(
                                        type = MenuAnchorType.PrimaryNotEditable, enabled = true
                                    ).fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandirCategoria,
                                    onDismissRequest = { expandirCategoria = false }
                                ) {
                                    categorias.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, color = TextoOperacion) },
                                            onClick = { categoria = cat; expandirCategoria = false }
                                        )
                                    }
                                }
                            }
                            CampoOperacion(
                                valorTxt, { valorTxt = it.filter(Char::isDigit) },
                                "Valor del gasto (COP)", singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            if (valorGasto > 0L) {
                                Surface(color = VerdeClaroOperacion, shape = RoundedCornerShape(12.dp)) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Valor a registrar", color = CafeOperacion)
                                        Text(monedaOperacion(valorGasto), color = VerdeOperacion, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            CampoOperacion(descripcionGasto, { descripcionGasto = it }, "Descripción del gasto")
                            CampoOperacion(tercero, { tercero = it }, "Tercero (opcional)", singleLine = true)
                            CampoOperacion(observacion, { observacion = it }, "Observación (opcional)")
                            Button(
                                onClick = {
                                    val id = operacionSeleccionadaId ?: return@Button
                                    viewModel.registrarGasto(
                                        operacionId = id,
                                        categoria = categoria.trim(),
                                        descripcion = descripcionGasto.trim().ifBlank { null },
                                        valor = valorGasto,
                                        tercero = tercero.trim().ifBlank { null },
                                        observacion = observacion.trim().ifBlank { null }
                                    )
                                    categoria = "Transporte"
                                    valorTxt = ""
                                    descripcionGasto = ""
                                    tercero = ""
                                    observacion = ""
                                },
                                enabled = operacionSeleccionada != null && categoria.isNotBlank() && valorGasto > 0L,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeOperacion, contentColor = Color.White),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Filled.ReceiptLong, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Guardar gasto", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                2, 3 -> {
                    val lista = if (tabIndex == 2) activas else cerradas
                    item {
                        TituloSeccionOperacion(
                            if (tabIndex == 2) "Operaciones activas" else "Operaciones cerradas",
                            "${lista.size} operación(es)"
                        )
                    }
                    if (lista.isEmpty()) {
                        item {
                            AvisoOperacion(
                                if (tabIndex == 2) "No hay operaciones activas."
                                else "Todavía no hay operaciones cerradas."
                            )
                        }
                    } else {
                        items(lista, key = { it.id }) { op ->
                            TarjetaResumenOperacion(
                                operacion = op,
                                onVerGastos = {
                                    tituloDetalle = nombreLegibleOperacion(op.nombre)
                                    viewModel.cargarGastosDeOperacion(op.id)
                                    mostrarDetalle = true
                                },
                                onCerrar = { operacionParaCerrar = op }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDetalle) {
        AlertDialog(
            onDismissRequest = { mostrarDetalle = false },
            containerColor = Color.White,
            title = { Text("Gastos de $tituloDetalle", color = CafeOperacion, fontWeight = FontWeight.Bold) },
            text = {
                if (gastosOperacionSeleccionada.isEmpty()) {
                    Text("Esta operación no tiene gastos registrados.", color = SubtextoOperacion)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                        items(gastosOperacionSeleccionada) { gasto ->
                            Surface(color = FondoOperacion, shape = RoundedCornerShape(12.dp)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(gasto.categoria, color = CafeOperacion, fontWeight = FontWeight.Bold)
                                    Text(monedaOperacion(gasto.valor), color = VerdeOperacion, fontWeight = FontWeight.Bold)
                                    if (!gasto.descripcion.isNullOrBlank()) Text(gasto.descripcion, color = TextoOperacion)
                                    if (!gasto.tercero.isNullOrBlank()) Text("Tercero: ${gasto.tercero}", color = SubtextoOperacion)
                                    if (!gasto.observacion.isNullOrBlank()) Text("Nota: ${gasto.observacion}", color = SubtextoOperacion)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDetalle = false }) { Text("Cerrar", color = VerdeOperacion) }
            }
        )
    }

    operacionParaCerrar?.let { op ->
        AlertDialog(
            onDismissRequest = { operacionParaCerrar = null },
            containerColor = Color.White,
            title = { Text("¿Cerrar operación?", color = CafeOperacion, fontWeight = FontWeight.Bold) },
            text = {
                Text("Se cerrará ${nombreLegibleOperacion(op.nombre)}. Confirma que ya no necesitas registrar más gastos en ella.",
                    color = TextoOperacion)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.cerrarOperacion(op.id); operacionParaCerrar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoOperacion, contentColor = Color.White)
                ) { Text("Cerrar operación") }
            },
            dismissButton = {
                TextButton(onClick = { operacionParaCerrar = null }) { Text("Cancelar", color = CafeOperacion) }
            }
        )
    }
}

@Composable
private fun ResumenOperacion(titulo: String, dato: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(horizontal = 9.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(dato, color = VerdeOperacion, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(titulo, color = SubtextoOperacion, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TarjetaOperacion(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp), content = content)
    }
}

@Composable
private fun TituloSeccionOperacion(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, color = CafeOperacion, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge)
        Text(subtitulo, color = SubtextoOperacion, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AvisoOperacion(mensaje: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Text(mensaje, modifier = Modifier.padding(20.dp), color = SubtextoOperacion,
            style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun coloresCampoOperacion() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextoOperacion,
    unfocusedTextColor = TextoOperacion,
    focusedLabelColor = CafeOperacion,
    unfocusedLabelColor = TextoOperacion,
    focusedPlaceholderColor = SubtextoOperacion,
    unfocusedPlaceholderColor = SubtextoOperacion,
    focusedBorderColor = VerdeOperacion,
    unfocusedBorderColor = BordeOperacion,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = VerdeOperacion
)

@Composable
private fun CampoOperacion(
    valor: String,
    onChange: (String) -> Unit,
    etiqueta: String,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        colors = coloresCampoOperacion(),
        shape = RoundedCornerShape(13.dp),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions
    )
}

@Composable
private fun TarjetaResumenOperacion(
    operacion: OperacionResumenUi,
    onVerGastos: () -> Unit,
    onCerrar: () -> Unit
) {
    val activa = operacion.estado == EstadoOperacion.ACTIVA
    TarjetaOperacion {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(if (activa) Icons.Filled.Inventory2 else Icons.Filled.CheckCircle,
                contentDescription = null, tint = if (activa) VerdeOperacion else CafeOperacion,
                modifier = Modifier.size(25.dp))
            Column(Modifier.weight(1f)) {
                Text(nombreLegibleOperacion(operacion.nombre), color = CafeOperacion,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(operacion.tipo, color = SubtextoOperacion,
                    style = MaterialTheme.typography.bodySmall)
            }
            Surface(color = if (activa) VerdeClaroOperacion else FondoOperacion,
                shape = RoundedCornerShape(30.dp)) {
                Text(if (activa) "Activa" else "Cerrada",
                    Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    color = if (activa) VerdeOperacion else CafeOperacion,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold)
            }
        }
        if (!operacion.descripcion.isNullOrBlank()) {
            Text(operacion.descripcion, color = SubtextoOperacion)
        }
        Surface(color = VerdeClaroOperacion, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total de gastos", color = CafeOperacion)
                Text(monedaOperacion(operacion.totalGastos), color = VerdeOperacion,
                    fontWeight = FontWeight.Bold)
            }
        }
        OutlinedButton(
            onClick = onVerGastos,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeOperacion)
        ) {
            Icon(Icons.Filled.History, null)
            Spacer(Modifier.width(8.dp))
            Text("Ver gastos")
        }
        if (activa) {
            Button(
                onClick = onCerrar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoOperacion, contentColor = Color.White)
            ) {
                Icon(Icons.Filled.Close, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar operación")
            }
        }
    }
}
