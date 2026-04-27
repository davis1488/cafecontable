package com.ethandev.cafecontable.ui.screen.utilidad

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.model.UtilidadOperacionModel
import com.ethandev.cafecontable.domain.model.UtilidadPendienteModel
import kotlin.math.roundToLong

@Composable
fun UtilidadScreen(
    viewModel: UtilidadViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.cargarTodo()
    }

    val state by viewModel.state.collectAsState()

    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pendientes", "Historial", "Resumen")

    LaunchedEffect(state.mensaje, state.error) {
        // Aquí puedes conectar Snackbar si ya tienes Scaffold global.
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "Utilidades",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = tabSeleccionado) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tabSeleccionado == index,
                    onClick = { tabSeleccionado = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        state.mensaje?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }

        when (tabSeleccionado) {
            0 -> PendientesUtilidadTab(
                pendientes = state.pendientes,
                onCalcular = { pendiente, nota ->
                    viewModel.calcularUtilidad(pendiente, nota)
                }
            )

            1 -> HistorialUtilidadTab(
                historial = state.historial
            )

            2 -> ResumenUtilidadTab(
                resumen = state.resumen
            )
        }
    }
}


@Composable
private fun PendientesUtilidadTab(
    pendientes: List<UtilidadPendienteModel>,
    onCalcular: (UtilidadPendienteModel, String?) -> Unit
) {
    if (pendientes.isEmpty()) {
        Text("No hay utilidades pendientes por calcular.")
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(pendientes) { item ->
            PendienteUtilidadCard(
                item = item,
                onCalcular = onCalcular
            )
        }
    }
}

//@Composable
//private fun PendienteUtilidadCard(
//    item: UtilidadPendienteModel,
//    onCalcular: (UtilidadPendienteModel, String?) -> Unit
//) {
//    var nota by remember { mutableStateOf("") }
//
//    val costoCafe = (item.cantidadKg * item.costoPromedioKg).roundToLong()
//    val utilidadBruta = item.valorVentaNeto - costoCafe
//
//    Card(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp)
//        ) {
//            Text(
//                text = item.cliente ?: "Cliente sin nombre",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            InfoRow("Cantidad kg", item.cantidadKg.toString())
//            InfoRow("Venta neta", formatMoney(item.valorVentaNeto))
//            InfoRow("Costo café", formatMoney(costoCafe))
//            InfoRow("Utilidad bruta estimada", formatMoney(utilidadBruta))
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            OutlinedTextField(
//                value = nota,
//                onValueChange = { nota = it },
//                label = { Text("Nota") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Button(
//                onClick = { onCalcular(item, nota.ifBlank { null }) },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Calcular utilidad")
//            }
//        }
//    }
//}

@Composable
private fun PendienteUtilidadCard(
    item: UtilidadPendienteModel,
    onCalcular: (UtilidadPendienteModel, String?) -> Unit
) {
    var nota by remember { mutableStateOf("") }

    val costoCafe = (item.cantidadKg * item.costoPromedioKg).roundToLong()
    val utilidadBruta = item.valorVentaNeto - costoCafe
    val utilidadNetaEstimada = utilidadBruta - item.totalGastos

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.cliente ?: "Cliente sin nombre",
                style = MaterialTheme.typography.titleMedium
            )

            InfoRow("Cantidad kg", item.cantidadKg.toString())
            InfoRow("Venta neta", formatMoney(item.valorVentaNeto))
            InfoRow("Costo café", formatMoney(costoCafe))
            InfoRow("Utilidad bruta estimada", formatMoney(utilidadBruta))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Gastos asociados",
                style = MaterialTheme.typography.titleSmall
            )

            InfoRow("Gastos compra", formatMoney(item.gastosCompra))
            InfoRow("Gastos mezcla", formatMoney(item.gastosMezcla))
            InfoRow("Gastos entrega", formatMoney(item.gastosEntrega))
            InfoRow("Total gastos", formatMoney(item.totalGastos))

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow("Utilidad neta estimada", formatMoney(utilidadNetaEstimada))

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Nota") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onCalcular(item, nota.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calcular utilidad")
            }
        }
    }
}


@Composable
private fun HistorialUtilidadTab(
    historial: List<UtilidadOperacionModel>
) {
    if (historial.isEmpty()) {
        Text("No hay utilidades registradas.")
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(historial) { item ->
            HistorialUtilidadCard(item)
        }
    }
}

@Composable
private fun HistorialUtilidadCard(
    item: UtilidadOperacionModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.cliente ?: "Cliente sin nombre",
                style = MaterialTheme.typography.titleMedium
            )

            InfoRow("Venta neta", formatMoney(item.valorVentaNeto))
            InfoRow("Costo café", formatMoney(item.costoCafe))
            InfoRow("Gastos", formatMoney(item.totalGastos))
            InfoRow("Utilidad bruta", formatMoney(item.utilidadBruta))
            InfoRow("Utilidad neta", formatMoney(item.utilidadNeta))
            InfoRow("Margen", "${"%.2f".format(item.margenPorcentaje)} %")

            item.nota?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Nota: $it")
            }
        }
    }
}

@Composable
private fun ResumenUtilidadTab(
    resumen: com.ethandev.cafecontable.domain.repository.ResumenUtilidadModel?
) {
    if (resumen == null) {
        Text("No hay resumen disponible.")
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Resumen general",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow("Ventas netas", formatMoney(resumen.ventasNetas))
            InfoRow("Costo café", formatMoney(resumen.costoCafe))
            InfoRow("Gastos", formatMoney(resumen.gastos))
            InfoRow("Utilidad neta", formatMoney(resumen.utilidadNeta))
            InfoRow("Margen", "${"%.2f".format(resumen.margenPorcentaje)} %")
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatMoney(value: Long): String {
    return "$" + "%,d".format(value).replace(",", ".")
}