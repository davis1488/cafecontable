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
import androidx.compose.material3.Button
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
import com.ethandev.cafecontable.domain.constants.EstadoAnuncio
import com.ethandev.cafecontable.domain.model.VentaPedido
import com.ethandev.cafecontable.domain.repository.VentaPedidoInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private enum class HistorialFiltro(val label: String, val estadoDb: String?) {
    TODOS("Todos", null),
    CREADO("Creado", "CREADO"),
    ENTREGA_PARCIAL("Entrega parcial", "ENTREGA_PARCIAL"),
    ENTREGA_TOTAL("Entrega total", "ENTREGA_TOTAL")
}



@Composable
fun VentasPedidoScreen(
    vm: VentasPedidoViewModel,
    onIrAsignacionMezcla: (String) -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }

    var cliente by remember { mutableStateOf("") }
    var cantidadTxt by remember { mutableStateOf("") }
    var precioTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }

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
                text = "Anuncios",
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
                    text = { Text("Nuevo anuncio") }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Historial") }
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
                    onIrAsignacionMezcla = onIrAsignacionMezcla
                )
            }
        }
    }
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
                text = "Registrar nuevo anuncio",
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
                Text(if (loading) "Guardando..." else "Registrar anuncio")
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
    onIrAsignacionMezcla: (String) -> Unit
) {
    var filtroSeleccionado by remember { mutableStateOf(HistorialFiltro.TODOS) }
    val itemsFiltrados = remember(items, filtroSeleccionado) {
        when (filtroSeleccionado) {
            HistorialFiltro.TODOS -> items

            HistorialFiltro.CREADO -> items.filter {
                EstadoAnuncio.from(it.estado) == EstadoAnuncio.CREADO
            }

            HistorialFiltro.ENTREGA_PARCIAL -> items.filter {
                EstadoAnuncio.from(it.estado) == EstadoAnuncio.ENTREGA_PARCIAL
            }

            HistorialFiltro.ENTREGA_TOTAL -> items.filter {
                EstadoAnuncio.from(it.estado) == EstadoAnuncio.ENTREGA_TOTAL
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Historial de anuncios",
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
                        text = "No hay anuncios en este filtro.",
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
                        onIrAsignacionMezcla = onIrAsignacionMezcla
                    )
                }
            }
        }
    }
}

@Composable
private fun VentaPedidoItem(
    item: VentaPedido,
    onIrAsignacionMezcla: (String) -> Unit
) {
    val fechaFormateada = remember(item.fecha) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(item.fecha))
    }

    val cantidadEntregada = item.cantidadAsignada
    val cantidadPendiente = (item.cantidadPactada - cantidadEntregada).coerceAtLeast(0.0)

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
            Text("Entregado: $cantidadEntregada ${item.unidad}")
            Text("Pendiente: $cantidadPendiente ${item.unidad}")
            Text("Estado: ${item.estado}")

            if (!item.nota.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Nota: ${item.nota}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onIrAsignacionMezcla(item.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Asignar mezcla")
            }
        }
    }
}

