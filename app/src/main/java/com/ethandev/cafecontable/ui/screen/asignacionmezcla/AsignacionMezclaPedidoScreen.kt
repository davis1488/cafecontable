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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
    var factor by remember { mutableStateOf("90") }
    var nota by remember { mutableStateOf("") }

    val pedidoSeleccionado = state.pedidosDisponibles.firstOrNull { it.pedidoId == pedidoId }
    val mezclaSeleccionada = state.mezclasDisponibles.firstOrNull { it.mezclaId == mezclaId }

    val precioUnitVenta = pedidoSeleccionado?.precioUnitVenta?.toString() ?: ""

    val subtotal = viewModel.calcularSubtotal(
        cantidadAsignada = cantidadAsignada.toDoubleOrNull() ?: 0.0,
        precioUnitVenta = precioUnitVenta.toLongOrNull() ?: 0L
    )

    val faltantePedido = if (pedidoSeleccionado != null) {
        ((pedidoSeleccionado.cantidadPedido ?: 0.0) - (pedidoSeleccionado.cantidadAsignada ?: 0.0))
            .coerceAtLeast(0.0)
    } else {
        0.0
    }

    val disponibleMezcla = mezclaSeleccionada?.cantidadDisponible ?: 0.0
    val cantidadAsignadaNum = cantidadAsignada.toDoubleOrNull() ?: 0.0

    val puedeGuardar =
        !state.loading &&
                pedidoSeleccionado != null &&
                mezclaSeleccionada != null &&
                cantidadAsignadaNum > 0.0 &&
                disponibleMezcla > 0.0 &&
                faltantePedido > 0.0 &&
                cantidadAsignadaNum <= disponibleMezcla &&
                cantidadAsignadaNum <= faltantePedido

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
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cargando anuncios y mezclas...")
            }
            Spacer(modifier = Modifier.height(12.dp))
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
                val faltante = ((pedido.cantidadPedido ?: 0.0) - (pedido.cantidadAsignada ?: 0.0))
                    .coerceAtLeast(0.0)

                val anuncioCompleto = faltante <= 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Cliente: ${pedido.clienteNombre}")
                        Text("Producto: ${pedido.productoNombre}")
                        Text("Cantidad pactada: ${pedido.cantidadPedido} kg")
                        Text("Cantidad asignada: ${pedido.cantidadAsignada} kg")
                        Text("Faltante: $faltante kg")
                        Text("Precio unitario venta: ${pedido.precioUnitVenta}")
                        Text("Pedido ID: ${pedido.pedidoId}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { pedidoId = pedido.pedidoId },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !anuncioCompleto && pedidoId != pedido.pedidoId
                        ) {
                            Text(
                                when {
                                    pedidoId == pedido.pedidoId -> "Anuncio seleccionado"
                                    anuncioCompleto -> "Anuncio completado"
                                    else -> "Seleccionar anuncio"
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mezclas disponibles para ligar",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.mezclasDisponibles.isEmpty()) {
            Text("No hay mezclas disponibles")
        } else {
            state.mezclasDisponibles.forEach { mezcla ->
                val agotada = mezcla.cantidadDisponible <= 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Descripción: ${mezcla.descripcion}")
                        Text("Estado: ${mezcla.estado}")
                        Text("Disponible: ${mezcla.cantidadDisponible} kg")
                        Text("Costo promedio/kg: ${mezcla.costoPromedioKg}")
                        Text("Mezcla ID: ${mezcla.mezclaId}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { mezclaId = mezcla.mezclaId },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !agotada && mezclaId != mezcla.mezclaId
                        ) {
                            Text(
                                when {
                                    mezclaId == mezcla.mezclaId -> "Mezcla seleccionada"
                                    agotada -> "Mezcla agotada"
                                    else -> "Seleccionar mezcla"
                                }
                            )
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
            onValueChange = { cantidadAsignada = it.replace(',', '.') },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cantidad asignada (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (mezclaSeleccionada != null && pedidoSeleccionado != null) {
            Text(
                text = "Disponible en mezcla: $disponibleMezcla kg\nFaltante de anuncio: $faltantePedido kg",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                cantidadAsignadaNum > disponibleMezcla -> {
                    Text(
                        text = "La cantidad supera lo disponible en la mezcla.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                cantidadAsignadaNum > faltantePedido -> {
                    Text(
                        text = "La cantidad supera el faltante del anuncio.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        OutlinedTextField(
            value = precioUnitVenta,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Precio unitario venta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            readOnly = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = factor,
            onValueChange = { factor = it.replace(',', '.') },
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
                    cantidadAsignada = cantidadAsignadaNum,
                    precioUnitVenta = pedidoSeleccionado?.precioUnitVenta ?: 0L,
                    factor = factor.toDoubleOrNull() ?: 90.0,
                    nota = nota
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = puedeGuardar
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Guardar asignación")
        }
    }
}