package com.ethandev.cafecontable.ui.screen.liquidacion

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ethandev.cafecontable.domain.model.LiquidacionHistorial
import com.ethandev.cafecontable.domain.model.LiquidacionPendiente
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun LiquidacionScreen(
    viewModel: LiquidacionViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pendientes", "Histórico")

    LaunchedEffect(state.error, state.okMsg) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
        state.okMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (state.loading && state.pendientes.isEmpty() && state.historial.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        when (selectedTab) {
            0 -> PendientesLiquidacionTab(
                items = state.pendientes,
                onGuardar = { item, descuentoCooperativa, otrosDescuentos, nota ->
                    viewModel.registrarLiquidacion(
                        item = item,
                        descuentoCooperativa = descuentoCooperativa,
                        otrosDescuentos = otrosDescuentos,
                        nota = nota
                    )
                }
            )
            1 -> HistorialLiquidacionTab(
                items = state.historial
            )
        }
    }
}

@Composable
private fun PendientesLiquidacionTab(
    items: List<LiquidacionPendiente>,
    onGuardar: (LiquidacionPendiente, Long, Long, String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Asignaciones pendientes por liquidar")
        }

        if (items.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay asignaciones pendientes por liquidar.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(items, key = { it.asignacionId }) { item ->
            PendienteLiquidacionCard(
                item = item,
                onGuardar = onGuardar
            )
        }
    }
}

@Composable
private fun PendienteLiquidacionCard(
    item: LiquidacionPendiente,
    onGuardar: (LiquidacionPendiente, Long, Long, String?) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    var descuentoCooperativaTxt by remember { mutableStateOf("0") }
    var otrosDescuentosTxt by remember { mutableStateOf("0") }
    var notaTxt by remember { mutableStateOf("") }

    val descuentoCooperativa = descuentoCooperativaTxt.toLongOrNull() ?: 0L
    val otrosDescuentos = otrosDescuentosTxt.toLongOrNull() ?: 0L

    val valorBase = (item.cantidadKg * item.precioBaseKg.toDouble()).roundToLong()
    val diferencia = item.factorReal - 90.0
    val ajusteFactor = ((valorBase * diferencia) / 100.0).roundToLong()
    val valorNeto = valorBase - ajusteFactor - descuentoCooperativa - otrosDescuentos

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cliente: ${item.cliente ?: "Sin cliente"}")
            Text("Pedido: ${item.pedidoId}")
            Text("Mezcla: ${item.mezclaId}")
            Text("Cantidad: ${formatDouble(item.cantidadKg)} kg")
            Text("Precio base: ${formatMoney(item.precioBaseKg)}")
            Text("Factor real: ${formatDouble(item.factorReal)}")

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(onClick = { expandido = !expandido }) {
                Text(if (expandido) "Ocultar" else "Liquidar")
            }

            if (expandido) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Valor base: ${formatMoney(valorBase)}")
                if (item.factorReal > 90.0) {
                    Text("Descuento por factor: ${formatMoney(ajusteFactor)}")
                } else if (item.factorReal < 90.0) {
                    Text("Bonificación por factor: ${formatMoney(abs(ajusteFactor))}")
                } else {
                    Text("Ajuste por factor: ${formatMoney(0)}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descuentoCooperativaTxt,
                    onValueChange = { descuentoCooperativaTxt = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descuento cooperativa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = otrosDescuentosTxt,
                    onValueChange = { otrosDescuentosTxt = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Otros descuentos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notaTxt,
                    onValueChange = { notaTxt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Neto a pagar: ${formatMoney(valorNeto)}")

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onGuardar(
                            item,
                            descuentoCooperativa,
                            otrosDescuentos,
                            notaTxt.ifBlank { null }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = valorNeto >= 0L
                ) {
                    Text("Guardar liquidación")
                }
            }
        }
    }
}

@Composable
private fun HistorialLiquidacionTab(
    items: List<LiquidacionHistorial>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Histórico de liquidaciones")
        }

        if (items.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay liquidaciones registradas.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(items, key = { it.id }) { item ->
            HistorialLiquidacionCard(item = item)
        }
    }
}

@Composable
private fun HistorialLiquidacionCard(
    item: LiquidacionHistorial
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Fecha: ${formatFecha(item.fecha)}")
            Text("Cliente: ${item.cliente ?: "Sin cliente"}")
            Text("Pedido: ${item.pedidoId}")
            Text("Mezcla: ${item.mezclaId}")
            Text("Asignación: ${item.asignacionId}")
            Text("Cantidad: ${formatDouble(item.cantidadKg)} kg")
            Text("Precio base: ${formatMoney(item.precioBaseKg)}")
            Text("Factor real: ${formatDouble(item.factorReal)}")
            Text("Valor base: ${formatMoney(item.valorBase)}")
            Text("Ajuste factor: ${formatMoney(item.ajusteFactor)}")
            Text("Descuento cooperativa: ${formatMoney(item.descuentoCooperativa)}")
            Text("Otros descuentos: ${formatMoney(item.otrosDescuentos)}")
            Text("Valor neto: ${formatMoney(item.valorNeto)}")
            if (!item.nota.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Nota: ${item.nota}")
            }
        }
    }
}

private fun formatMoney(value: Long): String {
    return "$ " + "%,d".format(value)
}

private fun formatDouble(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format("%.2f", value)
    }
}

private fun formatFecha(value: Long): String {
    return DateFormat.format("yyyy-MM-dd HH:mm", value).toString()
}