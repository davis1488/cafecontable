package com.ethandev.cafecontable.ui.screen.ventaspedido

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.VentaPedidoInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private enum class HistorialFiltro(val label: String) {
    TODOS("Todos"),
    PENDIENTE_ASIGNACION("Pendiente"),
    ASIGNACION_PARCIAL("Asignado Parcial"),
    ASIGNADO("Asignado"),
    ENTREGADO("Entregado"),
    ANALIZADO("Analizado"),
    FINALIZADO("Finalizado")
}

@Composable
fun VentasPedidoScreen(
    vm: VentasPedidoViewModel,
    onPrepararEntrega: (String) -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    var cliente by remember { mutableStateOf("") }
    var cantidadTxt by remember { mutableStateOf("") }
    var precioTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }

    // Dialogo entrega
    var showDialogEntregar by remember { mutableStateOf(false) }
    var pedidoIdEntregar by remember { mutableStateOf("") }
    var sacosTxt by remember { mutableStateOf("") }
    var pesoNetoTxt by remember { mutableStateOf("") }
    var pesoBrutoTxt by remember { mutableStateOf("") }

    // Dialogo analisis
    var showDialogAnalizar by remember { mutableStateOf(false) }
    var pedidoIdAnalizar by remember { mutableStateOf("") }
    var factorTxt by remember { mutableStateOf("90") }

    LaunchedEffect(Unit) {
        vm.cargarVentas()
    }

    LaunchedEffect(state.error, state.okMsg) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensajes()
        }

        state.okMsg?.let {
            snackbarHostState.showSnackbar(it)
            vm.limpiarMensajes()
        }
    }

    val cantidad = cantidadTxt.toDoubleOrNull() ?: 0.0
    val precio = precioTxt.toLongOrNull() ?: 0L
    val total = if (cantidad > 0 && precio > 0) {
        (cantidad * precio).toLong()
    } else {
        0L
    }

    val previewAnalisis = calcularAjusteAnalisis(
        precioBase = 100L,
        factor = factorTxt.toDoubleOrNull() ?: 90.0
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Anuncios / Entregas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Nuevo Anuncio") }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Estados de entrega") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> NuevoPedidoTab(
                    loading = state.loading,
                    cliente = cliente,
                    cantidadTxt = cantidadTxt,
                    precioTxt = precioTxt,
                    notaTxt = notaTxt,
                    total = total,
                    onClienteChange = { cliente = it },
                    onCantidadChange = { cantidadTxt = it.replace(',', '.') },
                    onPrecioChange = { precioTxt = it.filter(Char::isDigit) },
                    onNotaChange = { notaTxt = it },
                    onRegistrar = {
                        vm.registrar(
                            VentaPedidoInput(
                                cliente = cliente.trim(),
                                cantidadPactada = cantidad,
                                unidad = "KG",
                                precioUnitVenta = precio,
                                nota = notaTxt.trim().ifBlank { null }
                            )
                        )

                        cliente = ""
                        cantidadTxt = ""
                        precioTxt = ""
                        notaTxt = ""
                    }
                )

                1 -> HistorialPedidosTab(
                    loading = state.loading,
                    items = state.items,
                    onEntregar = { pedidoId ->
                        pedidoIdEntregar = pedidoId
                        sacosTxt = ""
                        pesoNetoTxt = ""
                        pesoBrutoTxt = ""
                        showDialogEntregar = true
                    },
                    onAnalizar = { pedidoId ->
                        pedidoIdAnalizar = pedidoId
                        factorTxt = "90"
                        showDialogAnalizar = true
                    },
                    onFinalizar = { pedidoId ->
                        vm.finalizarPedido(pedidoId)
                    }
                )
            }
        }
    }

    DialogEntregar(
        visible = showDialogEntregar,
        sacos = sacosTxt,
        pesoNeto = pesoNetoTxt,
        pesoBruto = pesoBrutoTxt,
        onSacosChange = { sacosTxt = it },
        onPesoNetoChange = { pesoNetoTxt = it.replace(',', '.') },
        onPesoBrutoChange = { pesoBrutoTxt = it.replace(',', '.') },
        onConfirm = {
            val sacos = sacosTxt.toIntOrNull() ?: 0
            val pesoNeto = pesoNetoTxt.toDoubleOrNull() ?: 0.0
            val pesoBruto = pesoBrutoTxt.toDoubleOrNull() ?: 0.0

            vm.marcarEntregado(
                pedidoId = pedidoIdEntregar,
                numeroSacos = sacos,
                pesoNeto = pesoNeto,
                pesoBruto = pesoBruto
            )

            showDialogEntregar = false
        },
        onDismiss = {
            showDialogEntregar = false
        }
    )

    DialogAnalizar(
        visible = showDialogAnalizar,
        factor = factorTxt,
        resumen = previewAnalisis,
        onFactorChange = { factorTxt = it },
        onConfirm = {
            val factor = factorTxt.toDoubleOrNull() ?: 90.0

            vm.marcarAnalizado(
                pedidoId = pedidoIdAnalizar,
                factor = factor
            )

            showDialogAnalizar = false
        },
        onDismiss = {
            showDialogAnalizar = false
        }
    )
}

@Composable
private fun NuevoPedidoTab(
    loading: Boolean,
    cliente: String,
    cantidadTxt: String,
    precioTxt: String,
    notaTxt: String,
    total: Long,
    onClienteChange: (String) -> Unit,
    onCantidadChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onNotaChange: (String) -> Unit,
    onRegistrar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Registrar nuevo pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = cliente,
                onValueChange = onClienteChange,
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = cantidadTxt,
                onValueChange = onCantidadChange,
                label = { Text("Cantidad pactada (KG)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = precioTxt,
                onValueChange = onPrecioChange,
                label = { Text("Precio venta unitario (COP)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notaTxt,
                onValueChange = onNotaChange,
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Total venta: $total COP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRegistrar,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Guardando..." else "Registrar Anuncio")
            }

            if (loading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HistorialPedidosTab(
    loading: Boolean,
    items: List<VentaPedido>,
    onEntregar: (String) -> Unit,
    onAnalizar: (String) -> Unit,
    onFinalizar: (String) -> Unit
) {
    var filtroSeleccionado by remember { mutableStateOf(HistorialFiltro.TODOS) }

    val itemsFiltrados = remember(items, filtroSeleccionado) {
        when (filtroSeleccionado) {
            HistorialFiltro.TODOS -> items
            HistorialFiltro.ANALIZADO -> items.filter {
                it.estado.equals("ANALIZADO", ignoreCase = true)
            }
            HistorialFiltro.ENTREGADO -> items.filter {
                it.estado.equals("ENTREGADO", ignoreCase = true)
            }
            HistorialFiltro.PENDIENTE_ASIGNACION -> items.filter {
                it.estado.equals("PENDIENTE_ASIGNACION", ignoreCase = true)
            }
            HistorialFiltro.ASIGNACION_PARCIAL -> items.filter {
                it.estado.equals("ASIGNACION_PARCIAL", ignoreCase = true)
            }
            HistorialFiltro.ASIGNADO -> items.filter {
                it.estado.equals("ASIGNADO", ignoreCase = true)
            }
            HistorialFiltro.FINALIZADO -> items.filter {
                it.estado.equals("FINALIZADO", ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Historial de pedidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = filtroSeleccionado.ordinal,
            edgePadding = 0.dp
        ) {
            HistorialFiltro.entries.forEach { filtro ->
                Tab(
                    selected = filtroSeleccionado == filtro,
                    onClick = { filtroSeleccionado = filtro },
                    text = { Text(filtro.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Mostrando: ${filtroSeleccionado.label}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${itemsFiltrados.size} registros",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loading && items.isEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (itemsFiltrados.isEmpty() && !loading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "No hay pedidos en este filtro.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itemsFiltrados, key = { it.id }) { item ->
                    VentaPedidoItem(
                        item = item,
                        onEntregar = onEntregar,
                        onAnalizar = onAnalizar,
                        onFinalizar = onFinalizar
                    )
                }
            }
        }
    }
}

@Composable
private fun VentaPedidoItem(
    item: VentaPedido,
    onEntregar: (String) -> Unit,
    onAnalizar: (String) -> Unit,
    onFinalizar: (String) -> Unit
) {
    val fechaFormateada = remember(item.fecha) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(item.fecha))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = item.cliente,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Fecha: $fechaFormateada")
            Text("Cantidad pactada: ${item.cantidadPactada} ${item.unidad}")
            Text("Precio unitario: ${item.precioUnitVenta} COP")
            Text("Total: ${item.totalVenta} COP")
            Text("Entregado: ${item.cantidadAsignada} ${item.unidad}")
            Text("Estado: ${item.estado}")

            if (!item.nota.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Nota: ${item.nota}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when {
                    item.estado.equals("PENDIENTE_ASIGNACION", ignoreCase = true) ||
                            item.estado.equals("ASIGNACION_PARCIAL", ignoreCase = true) -> {
                        Button(onClick = { onEntregar(item.id) }) {
                            Text("Pasar a entregado")
                        }
                    }

                    item.estado.equals("ENTREGADO", ignoreCase = true) -> {
                        Button(onClick = { onAnalizar(item.id) }) {
                            Text("Pasar a analizado")
                        }
                    }

                    item.estado.equals("ANALIZADO", ignoreCase = true) ||
                            item.estado.equals("ASIGNADO", ignoreCase = true) -> {
                        Button(onClick = { onFinalizar(item.id) }) {
                            Text("Finalizar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogEntregar(
    visible: Boolean,
    sacos: String,
    pesoNeto: String,
    pesoBruto: String,
    onSacosChange: (String) -> Unit,
    onPesoNetoChange: (String) -> Unit,
    onPesoBrutoChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar entrega") },
        text = {
            Column {
                OutlinedTextField(
                    value = sacos,
                    onValueChange = { onSacosChange(it.filter(Char::isDigit)) },
                    label = { Text("Número de sacos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pesoNeto,
                    onValueChange = onPesoNetoChange,
                    label = { Text("Peso neto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pesoBruto,
                    onValueChange = onPesoBrutoChange,
                    label = { Text("Peso bruto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogAnalizar(
    visible: Boolean,
    factor: String,
    resumen: String,
    onFactorChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar análisis") },
        text = {
            Column {
                OutlinedTextField(
                    value = factor,
                    onValueChange = onFactorChange,
                    label = { Text("Factor análisis") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = resumen,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun calcularAjusteAnalisis(
    precioBase: Long,
    factor: Double
): String {
    val diferencia = abs(factor - 90.0)
    return when {
        factor > 90.0 -> {
            val descuento = (precioBase * (diferencia / 100.0)).toLong()
            "Descuento estimado: $descuento COP (${diferencia.toInt()}%)"
        }
        factor < 90.0 -> {
            val bonificacion = (precioBase * (diferencia / 100.0)).toLong()
            "Bonificación estimada: $bonificacion COP (${diferencia.toInt()}%)"
        }
        else -> {
            "Sin ajuste por análisis"
        }
    }
}