package com.ethandev.cafecontable.ui.screen.historialcompras

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialComprasScreen(vm: HistorialComprasViewModel) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.cargar() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
//            Spacer(Modifier.height(8.dp))
        }

        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
//            Spacer(Modifier.height(12.dp))
        }

        if (!state.loading && state.items.isEmpty()) {
            Text("No hay compras registradas.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.items) { item ->
                val fecha = remember(item.fecha) {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.fecha))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.productoNombre, style = MaterialTheme.typography.titleMedium)
                        Text("Fecha: $fecha")
                        Text("Cantidad: ${item.cantidad} ${item.unidad}")
                        Text("Precio compra: ${item.precioUnitCompra} COP")
                        Text("Total: ${item.total} COP")
                        Text("Proveedor: ${item.proveedor ?: "—"}")
                        Text("Crédito: ${if (item.esCredito) "Sí" else "No"}")
                        if (!item.nota.isNullOrBlank()) {
                            Text("Nota: ${item.nota}")
                        }
                    }
                }
            }
        }
    }
}