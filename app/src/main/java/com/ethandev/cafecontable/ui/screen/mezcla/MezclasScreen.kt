package com.ethandev.cafecontable.ui.screen.mezcla

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.constants.EstadoMezcla
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MezclasScreen(
    viewModel: MezclasViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val items = remember { mutableStateListOf<MezclaFormItem>() }
    val fechaActual = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var nota by remember {
        mutableStateOf("MEZCLA $fechaActual")
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.cargarComprasDisponibles()
        viewModel.cargarHistorialMezclas()
    }

    LaunchedEffect(state.okMsg, state.error) {
        state.okMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
            items.clear()
            nota = "MEZCLA $fechaActual"
            viewModel.cargarHistorialMezclas()
        }

        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Mezclas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Registrar mezcla") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Historial / estado") }
            )
        }

        when (selectedTab) {
            0 -> RegistrarMezclaTab(
                state = state,
                items = items,
                nota = nota,
                onNotaChange = { nota = it },
                onSeleccionarOperacion = viewModel::seleccionarOperacionMezcla,
                onAgregarCompra = { compra ->
                    val yaAgregada = items.any { it.compraId == compra.compraId }
                    if (yaAgregada) {
                        Toast.makeText(
                            context,
                            "Esa compra ya fue agregada",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        items.add(viewModel.crearItemDesdeCompra(compra))
                    }
                },
                onEliminarItem = { index ->
                    items.removeAt(index)
                },
                onCantidadChange = { index, nuevaCantidad ->
                    items[index] = items[index].copy(cantidadUsada = nuevaCantidad)
                },
                calcularCantidadTotal = { viewModel.calcularCantidadTotal(items) },
                calcularCostoTotal = { viewModel.calcularCostoTotal(items) },
                calcularCostoPromedioKg = { viewModel.calcularCostoPromedioKg(items) },
                onGuardar = {
                    viewModel.registrarMezcla(
                        items = items.toList(),
                        nota = nota
                    )
                }
            )

            1 -> HistorialMezclasTab(
                state = state,
                onMarcarPendienteEntrega = { mezclaId, numeroSacos, kilajeEnviado ->
                    viewModel.marcarPendienteEntrega(
                        mezclaId = mezclaId,
                        numeroSacosEnviados = numeroSacos,
                        kilajeEnviado = kilajeEnviado
                    )
                },
                onMarcarEntregado = { mezclaId, numeroSacos, kilajeEntregado, lugarEntrega ->
                    viewModel.marcarEntregado(
                        mezclaId = mezclaId,
                        numeroSacosEntregados = numeroSacos,
                        kilajeEntregado = kilajeEntregado,
                        lugarEntrega = lugarEntrega
                    )
                },
                onMarcarAnalizado = { mezclaId, factor ->
                    viewModel.marcarAnalizado(
                        mezclaId = mezclaId,
                        factorRendimiento = factor
                    )
                }
            )
        }
    }
}

@Composable
private fun RegistrarMezclaTab(
    state: MezclasState,
    items: List<MezclaFormItem>,
    nota: String,
    onNotaChange: (String) -> Unit,
    onSeleccionarOperacion: (String) -> Unit,
    onAgregarCompra: (CompraDisponibleUi) -> Unit,
    onEliminarItem: (Int) -> Unit,
    onCantidadChange: (Int, Double) -> Unit,
    calcularCantidadTotal: () -> Double,
    calcularCostoTotal: () -> Long,
    calcularCostoPromedioKg: () -> Long,
    onGuardar: () -> Unit
) {
    val cantidadTotal = calcularCantidadTotal()
    val costoTotal = calcularCostoTotal()
    val costoPromedio = calcularCostoPromedioKg()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        SectionHeader(
            titulo = "Operación de mezcla",
            subtitulo = "Selecciona a qué operación pertenece esta mezcla"
        )

        Spacer(modifier = Modifier.height(10.dp))

        SelectOperacionMezcla(
            state = state,
            onSeleccionarOperacion = onSeleccionarOperacion
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(
            titulo = "Compras disponibles",
            subtitulo = "Selecciona una compra activa para agregarla a la mezcla"
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (state.loadingCompras) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Cargando compras disponibles...")
                }
            }
        } else if (state.comprasDisponibles.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "No hay compras activas disponibles para mezcla",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.comprasDisponibles.forEach { compra ->
                    val yaAgregada = items.any { it.compraId == compra.compraId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = compra.productoNombre,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoRow("Compra ID", compra.compraId)
                            InfoRow("Disponible", "${compra.cantidadDisponible} kg")
                            InfoRow("Costo unitario", "$${compra.costoUnitCompra}")

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onAgregarCompra(compra) },
                                enabled = !yaAgregada,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (yaAgregada) "Ya agregada" else "Agregar a mezcla")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(
            titulo = "Mezcla actual",
            subtitulo = "Aquí editas las cantidades de cada compra agregada"
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Todavía no has agregado compras a la mezcla",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val compraOriginal = state.comprasDisponibles.firstOrNull { it.compraId == item.compraId }

                    var cantidadTxt by remember(item.compraId) {
                        mutableStateOf(item.cantidadUsada.toString())
                    }

                    LaunchedEffect(item.cantidadUsada) {
                        val nuevoValor = item.cantidadUsada.toString()
                        if (cantidadTxt != nuevoValor) {
                            cantidadTxt = nuevoValor
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Item ${index + 1}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(onClick = { onEliminarItem(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar item"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                tonalElevation = 2.dp,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    InfoRow("Producto", item.productoNombre)
                                    InfoRow("Compra ID", item.compraId)
                                    InfoRow("Costo unitario", "$${item.costoUnitCompra}")
                                    InfoRow("Disponible", "${compraOriginal?.cantidadDisponible ?: 0.0} kg")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = cantidadTxt,
                                onValueChange = { value ->
                                    cantidadTxt = value
                                    val nuevaCantidad = value.toDoubleOrNull() ?: 0.0
                                    onCantidadChange(index, nuevaCantidad)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Cantidad usada (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (compraOriginal != null && item.cantidadUsada > compraOriginal.cantidadDisponible) {
                                Text(
                                    text = "La cantidad usada no puede ser mayor a la disponible",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = "Subtotal: ${((item.cantidadUsada * item.costoUnitCompra.toDouble())).toLong()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(
            titulo = "Resumen de la mezcla",
            subtitulo = "Totales calculados automáticamente"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Cantidad total", "$cantidadTotal kg")
                InfoRow("Costo total", "$$costoTotal")
                InfoRow("Costo promedio/kg", "$$costoPromedio")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nota,
            onValueChange = onNotaChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nota") },
            minLines = 3
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading && items.isNotEmpty()
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Guardar mezcla")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
private fun SectionHeader(
    titulo: String,
    subtitulo: String
) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectOperacionMezcla(
    state: MezclasState,
    onSeleccionarOperacion: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val operacionSeleccionada = state.operacionesMezcla
        .firstOrNull { it.id == state.operacionMezclaIdSeleccionada }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = operacionSeleccionada?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Operación") },
            placeholder = { Text("Selecciona una operación") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            state.operacionesMezcla.forEach { operacion ->
                DropdownMenuItem(
                    text = { Text(operacion.nombre) },
                    onClick = {
                        onSeleccionarOperacion(operacion.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HistorialMezclasTab(
    state: MezclasState,
    onMarcarPendienteEntrega: (String, Int, Double) -> Unit,
    onMarcarEntregado: (String, Int, Double, String) -> Unit,
    onMarcarAnalizado: (String, Double) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var mezclaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var mostrarDialogPendiente by remember { mutableStateOf(false) }
    var mostrarDialogEntregado by remember { mutableStateOf(false) }
    var mostrarDialogAnalizado by remember { mutableStateOf(false) }

    val tabs = listOf(
        EstadoMezcla.CREADO,
        EstadoMezcla.PENDIENTE_ENTREGA,
        EstadoMezcla.ENTREGADO,
        EstadoMezcla.ANALIZADO
    )

    val estadoSeleccionado = tabs[selectedTab]

    val mezclasFiltradas = state.historialMezclas.filter {
        it.estado.equals(estadoSeleccionado.name, ignoreCase = true)
    }

    if (mostrarDialogPendiente && mezclaSeleccionadaId != null) {
        PendienteEntregaDialog(
            onDismiss = {
                mostrarDialogPendiente = false
                mezclaSeleccionadaId = null
            },
            onConfirm = { numeroSacos, kilajeEnviado ->
                onMarcarPendienteEntrega(
                    mezclaSeleccionadaId!!,
                    numeroSacos,
                    kilajeEnviado
                )
                mostrarDialogPendiente = false
                mezclaSeleccionadaId = null
            }
        )
    }

    if (mostrarDialogEntregado && mezclaSeleccionadaId != null) {
        EntregadoDialog(
            onDismiss = {
                mostrarDialogEntregado = false
                mezclaSeleccionadaId = null
            },
            onConfirm = { numeroSacos, kilaje, lugar ->
                onMarcarEntregado(
                    mezclaSeleccionadaId!!,
                    numeroSacos,
                    kilaje,
                    lugar
                )
                mostrarDialogEntregado = false
                mezclaSeleccionadaId = null
            }
        )
    }

    if (mostrarDialogAnalizado && mezclaSeleccionadaId != null) {
        AnalizadoDialog(
            onDismiss = {
                mostrarDialogAnalizado = false
                mezclaSeleccionadaId = null
            },
            onConfirm = { factor ->
                onMarcarAnalizado(
                    mezclaSeleccionadaId!!,
                    factor
                )
                mostrarDialogAnalizado = false
                mezclaSeleccionadaId = null
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, estado ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(estado.name) }
                )
            }
        }

        if (state.loadingHistorial) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Cargando mezclas...")
            }
            return@Column
        }

        if (mezclasFiltradas.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No hay mezclas en estado $estadoSeleccionado",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(mezclasFiltradas) { index, mezcla ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Mezcla ${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow("ID", mezcla.id)
                        InfoRow("Fecha", mezcla.fechaTexto)
                        InfoRow("Cantidad total", "${mezcla.cantidadTotal} kg")
                        InfoRow("Costo total", "$${mezcla.costoTotal}")

                        Spacer(modifier = Modifier.height(8.dp))
                        EstadoMezclaChip(estado = mezcla.estado)

                        if (!mezcla.nota.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nota: ${mezcla.nota}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AccionesEstadoMezcla(
                            estadoActual = mezcla.estado,
                            onIrPendienteEntrega = {
                                mezclaSeleccionadaId = mezcla.id
                                mostrarDialogPendiente = true
                            },
                            onIrEntregado = {
                                mezclaSeleccionadaId = mezcla.id
                                mostrarDialogEntregado = true
                            },
                            onIrAnalizado = {
                                mezclaSeleccionadaId = mezcla.id
                                mostrarDialogAnalizado = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoMezclaChip(estado: String) {

    val (colorFondo, colorTexto) = when (estado.uppercase()) {
        EstadoMezcla.CREADO.toString() -> Pair(
            Color(0xFFE3F2FD), // azul claro
            Color(0xFF1565C0)
        )

        EstadoMezcla.ANALIZADO.toString() -> Pair(
            Color(0xFFFFF3E0), // naranja claro
            Color(0xFFE65100)
        )

        EstadoMezcla.ENTREGADO.toString() -> Pair(
            Color(0xFFE8F5E9), // verde claro
            Color(0xFF2E7D32)
        )

        else -> Pair(Color.LightGray, Color.Black)
    }

    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = estado,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = colorTexto,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AccionesEstadoMezcla(
    estadoActual: String,
    onIrPendienteEntrega: () -> Unit,
    onIrEntregado: () -> Unit,
    onIrAnalizado: () -> Unit
) {
    when (estadoActual.uppercase()) {
        EstadoMezcla.CREADO.toString() -> {
            Button(
                onClick = onIrPendienteEntrega,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar")
            }
        }

        EstadoMezcla.PENDIENTE_ENTREGA.toString() -> {
            Button(
                onClick = onIrEntregado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar entrega")
            }
        }

        EstadoMezcla.ENTREGADO.toString() -> {
            Button(
                onClick = onIrAnalizado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar análisis")
            }
        }

        EstadoMezcla.ANALIZADO.toString() -> {
            Text(
                text = "Proceso finalizado",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun PendienteEntregaDialog(
    onDismiss: () -> Unit,
    onConfirm: (numeroSacos: Int, kilajeEnviado: Double) -> Unit
) {
    var numeroSacosTxt by remember { mutableStateOf("") }
    var kilajeEnviadoTxt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pasar a pendiente de entrega") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = numeroSacosTxt,
                    onValueChange = { numeroSacosTxt = it },
                    label = { Text("Número de sacos enviados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = kilajeEnviadoTxt,
                    onValueChange = { kilajeEnviadoTxt = it.replace(',', '.') },
                    label = { Text("Kilaje enviado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val numeroSacos = numeroSacosTxt.toIntOrNull() ?: 0
                    val kilajeEnviado = kilajeEnviadoTxt.toDoubleOrNull() ?: 0.0

                    if (numeroSacos > 0 && kilajeEnviado > 0.0) {
                        onConfirm(numeroSacos, kilajeEnviado)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton (onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EntregadoDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        numeroSacosEntregados: Int,
        kilajeEntregado: Double,
        lugarEntrega: String
    ) -> Unit
) {
    var numeroSacosTxt by remember { mutableStateOf("") }
    var kilajeEntregadoTxt by remember { mutableStateOf("") }
    var lugarEntrega by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar entrega") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = numeroSacosTxt,
                    onValueChange = { numeroSacosTxt = it },
                    label = { Text("Número de sacos entregados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = kilajeEntregadoTxt,
                    onValueChange = { kilajeEntregadoTxt = it.replace(',', '.') },
                    label = { Text("Kilaje entregado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lugarEntrega,
                    onValueChange = { lugarEntrega = it },
                    label = { Text("Lugar de entrega") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val numeroSacos = numeroSacosTxt.toIntOrNull() ?: 0
                    val kilajeEntregado = kilajeEntregadoTxt.toDoubleOrNull() ?: 0.0
                    val lugar = lugarEntrega.trim()

                    if (numeroSacos > 0 && kilajeEntregado > 0.0 && lugar.isNotBlank()) {
                        onConfirm(numeroSacos, kilajeEntregado, lugar)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AnalizadoDialog(
    onDismiss: () -> Unit,
    onConfirm: (factorRendimiento: Double) -> Unit
) {
    var factorTxt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar análisis") },
        text = {
            Column {
                OutlinedTextField(
                    value = factorTxt,
                    onValueChange = { factorTxt = it.replace(',', '.') },
                    label = { Text("Factor de rendimiento") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val factor = factorTxt.toDoubleOrNull() ?: 0.0
                    if (factor > 0.0) {
                        onConfirm(factor)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

