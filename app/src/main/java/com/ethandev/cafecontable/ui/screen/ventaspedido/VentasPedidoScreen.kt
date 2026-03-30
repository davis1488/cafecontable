package com.ethandev.cafecontable.ui.screen.ventaspedido

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.repository.VentaPedidoInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VentasPedidoScreen(vm: VentasPedidoViewModel) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
    val total = if (cantidad > 0 && precio > 0) (cantidad * precio).toLong() else 0L

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Ventas / pedidos",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = cantidadTxt,
                onValueChange = { cantidadTxt = it.replace(',', '.') },
                label = { Text("Cantidad pactada (KG)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = precioTxt,
                onValueChange = { precioTxt = it.filter(Char::isDigit) },
                label = { Text("Precio venta unitario") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notaTxt,
                onValueChange = { notaTxt = it },
                label = { Text("Nota") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total venta: $total COP",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    vm.registrar(
                        VentaPedidoInput(
                            cliente = cliente,
                            cantidadPactada = cantidad,
                            unidad = "KG",
                            precioUnitVenta = precio,
                            nota = notaTxt.ifBlank { null }
                        )
                    )

                    cliente = ""
                    cantidadTxt = ""
                    precioTxt = ""
                    notaTxt = ""
                },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.loading) "Guardando..." else "Registrar venta")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    val fecha = remember(item.fecha) {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(item.fecha))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(item.cliente, style = MaterialTheme.typography.titleMedium)
                            Text("Fecha: $fecha")
                            Text("Cantidad pactada: ${item.cantidadPactada} ${item.unidad}")
                            Text("Precio unitario: ${item.precioUnitVenta} COP")
                            Text("Total: ${item.totalVenta} COP")
                            Text("Entregado: ${item.cantidadEntregada} ${item.unidad}")
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
}