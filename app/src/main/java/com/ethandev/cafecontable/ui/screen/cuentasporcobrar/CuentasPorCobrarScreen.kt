package com.ethandev.cafecontable.ui.screen.cuentasporcobrar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CuentasPorCobrarScreen(vm: CuentasPorCobrarViewModel) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.cargar() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!state.loading && state.items.isEmpty()) {
            Text("No hay cuentas por cobrar pendientes.")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.items) { item ->
                val fecha = remember(item.fecha) {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(item.fecha))
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = item.cliente,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Fecha: $fecha")
                        Text("Valor inicial: ${item.valorInicial} COP")
                        Text("Saldo pendiente: ${item.saldoPendiente} COP")
                        Text("Estado: ${item.estado}")
                        if (!item.nota.isNullOrBlank()) {
                            Text("Nota: ${item.nota}")
                        }
                    }
                }
            }
        }
    }
}