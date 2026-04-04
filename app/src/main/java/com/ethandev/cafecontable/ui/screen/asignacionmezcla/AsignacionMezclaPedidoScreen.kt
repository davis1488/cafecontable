package com.ethandev.cafecontable.ui.screen.asignacionmezcla

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AsignacionMezclaPedidoScreen(
    viewModel: AsignacionMezclaPedidoViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var pedidoId by remember { mutableStateOf("") }
    var mezclaId by remember { mutableStateOf("") }
    var cantidadAsignada by remember { mutableStateOf("") }
    var precioUnitVenta by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("90") }
    var nota by remember { mutableStateOf("") }

    val pedidoSeleccionado = state.pedidosDisponibles.firstOrNull { it.pedidoId == pedidoId }
    val mezclaSeleccionada = state.mezclasDisponibles.firstOrNull { it.mezclaId == mezclaId }

    val subtotal = viewModel.calcularSubtotal(
        cantidadAsignada = cantidadAsignada.toDoubleOrNull() ?: 0.0,
        precioUnitVenta = precioUnitVenta.toLongOrNull() ?: 0L
    )

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }


    LaunchedEffect(state.okMsg, state.error) {
        state.okMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()

            pedidoId = ""
            mezclaId = ""
            cantidadAsignada = ""
            precioUnitVenta = "${pedidoSeleccionado?.precioUnitVenta}"
            factor = "90"
            nota = ""
        }

        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Asignar mezcla a anuncio",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.loadingDatos) {
            Row(modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cargando anuncios y mezclas...")
            }
        }

        Text(
            text = "Anuncios disponibles",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.pedidosDisponibles.isEmpty()) {
            Text("No hay anuncios disponibles")
        } else {
            state.pedidosDisponibles.forEach { pedido ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Cliente: ${pedido.clienteNombre}")
                        Text("Producto: ${pedido.productoNombre}")
                        Text("Cantidad: ${pedido.cantidadPedido} kg")
                        Text("CantidadAsignada: ${pedido.cantidadAsignada} kg")
                        Text("PrecioUnitVenta: ${pedido.precioUnitVenta} kg")

                        Text("Pedido ID: ${pedido.pedidoId}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { pedidoId = pedido.pedidoId },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = pedidoId != pedido.pedidoId
                        ) {
                            Text(if (pedidoId == pedido.pedidoId) "Anuncio seleccionado" else "Seleccionar anuncio")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mezclas disponibles",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.mezclasDisponibles.isEmpty()) {
            Text("No hay mezclas disponibles")
        } else {
            state.mezclasDisponibles.forEach { mezcla ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Descripción: ${mezcla.descripcion}")
                        Text("Disponible: ${mezcla.cantidadDisponible} kg")
                        Text("Costo promedio/kg: ${mezcla.costoPromedioKg}")
                        Text("Mezcla ID: ${mezcla.mezclaId}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { mezclaId = mezcla.mezclaId },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = mezclaId != mezcla.mezclaId
                        ) {
                            Text(if (mezclaId == mezcla.mezclaId) "Mezcla seleccionada" else "Seleccionar mezcla")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Asignación",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Anuncio seleccionado: ${pedidoSeleccionado?.clienteNombre ?: "Ninguno"}")
                Text("Mezcla seleccionada: ${mezclaSeleccionada?.descripcion ?: "Ninguna"}")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = cantidadAsignada,
            onValueChange = { cantidadAsignada = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cantidad asignada (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (mezclaSeleccionada != null && pedidoSeleccionado != null) {

            val faltante = ((pedidoSeleccionado.cantidadPedido ?: 0.0) -
                    (pedidoSeleccionado.cantidadAsignada ?: 0.0))
                .coerceAtLeast(0.0)

            Text(
                text = "Disponible en mezcla: ${mezclaSeleccionada.cantidadDisponible} Kg\n" +
                        "Faltante de anuncio: $faltante Kg",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = precioUnitVenta,
            onValueChange = { precioUnitVenta = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Precio unitario venta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = factor,
            onValueChange = { factor = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Factor") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Subtotal: $subtotal",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = nota,
            onValueChange = { nota = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nota") },
            minLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.asignar(
                    pedidoId = pedidoId,
                    mezclaId = mezclaId,
                    cantidadAsignada = cantidadAsignada.toDoubleOrNull() ?: 0.0,
                    precioUnitVenta = pedidoSeleccionado?.precioUnitVenta ?: 0L,
                    factor = factor.toDoubleOrNull() ?: 90.0,
                    nota = nota
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Guardar asignación")
        }
    }
}