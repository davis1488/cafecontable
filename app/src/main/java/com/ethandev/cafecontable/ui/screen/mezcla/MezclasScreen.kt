package com.ethandev.cafecontable.ui.screen.mezcla

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun MezclasScreen(
    viewModel: MezclasViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val items = remember { mutableStateListOf<MezclaFormItem>() }
    var nota by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarComprasDisponibles()
    }

    LaunchedEffect(state.okMsg, state.error) {
        state.okMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
            items.clear()
            nota = ""
        }

        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
        }
    }

    val cantidadTotal = viewModel.calcularCantidadTotal(items)
    val costoTotal = viewModel.calcularCostoTotal(items)
    val costoPromedio = viewModel.calcularCostoPromedioKg(items)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Registrar mezcla",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    CircularProgressIndicator(modifier = Modifier.width(22.dp), strokeWidth = 2.dp)
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
                                onClick = {
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

                                IconButton(onClick = { items.removeAt(index) }) {
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
                                value = if (item.cantidadUsada == 0.0) "" else item.cantidadUsada.toString(),
                                onValueChange = { value ->
                                    val nuevaCantidad = value.toDoubleOrNull() ?: 0.0
                                    items[index] = item.copy(cantidadUsada = nuevaCantidad)
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
                                text = "Subtotal: ${(item.cantidadUsada * item.costoUnitCompra.toDouble()).toLong()}",
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
            onValueChange = { nota = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nota") },
            minLines = 3
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                viewModel.registrarMezcla(
                    items = items.toList(),
                    nota = nota
                )
            },
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