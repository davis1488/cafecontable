package com.ethandev.cafecontable.ui.screen.historialcompras

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialComprasScreen(vm: HistorialComprasViewModel) {
    val state by vm.state.collectAsState()

    val toastMessage by vm.toastMessage.collectAsState()
    val context = LocalContext.current

    val mostrarDialogoEditar = remember { mutableStateOf(false) }
    val compraEditar = remember { mutableStateOf<CompraHistorialItem?>(null) }

    val proveedorEditar = remember { mutableStateOf("") }
    val cantidadEditar = remember { mutableStateOf("") }
    val precioEditar = remember { mutableStateOf("") }
    val notaEditar = remember { mutableStateOf("") }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            vm.clearToastMessage()
        }
    }

    val mostrarDialogoAnular = remember { mutableStateOf(false) }
    val compraSeleccionadaId = remember { mutableStateOf<Int?>(null) }
    val compraSeleccionadaNombre = remember { mutableStateOf("") }


    LaunchedEffect(Unit) { vm.cargar() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
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
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(item.fecha))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = item.productoNombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Fecha: $fecha")
                        Text("Cantidad: ${item.cantidad} ${item.unidad}")
                        Text("Precio compra: ${item.precioUnitCompra} COP")
                        Text("Total: ${item.total} COP")
                        Text("Proveedor: ${item.proveedor ?: "—"}")
                        Text("Crédito: ${if (item.esCredito) "Sí" else "No"}")
                        Text("Estado: ${item.estado}")

                        if (!item.nota.isNullOrBlank()) {
                            Text("Nota: ${item.nota}")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    compraEditar.value = item
                                    proveedorEditar.value = item.proveedor ?: ""
                                    cantidadEditar.value = item.cantidad.toString()
                                    precioEditar.value = item.precioUnitCompra.toString()
                                    notaEditar.value = item.nota ?: ""
                                    mostrarDialogoEditar.value = true
                                },
                                modifier = Modifier.weight(1f),
                                enabled = item.estado != "ANULADA"
                            ) {
                                Text("Editar")
                            }

                            OutlinedButton(
                                onClick = {
                                    compraSeleccionadaId.value = item.id
                                    compraSeleccionadaNombre.value = item.productoNombre
                                    mostrarDialogoAnular.value = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Anular")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAnular.value && compraSeleccionadaId.value != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoAnular.value = false
            },
            title = {
                Text("Anular compra")
            },
            text = {
                Text("¿Deseas anular la compra de ${compraSeleccionadaNombre.value}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.anularCompra(compraSeleccionadaId.value!!)
                        mostrarDialogoAnular.value = false
                    }
                ) {
                    Text("Sí, anular")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoAnular.value = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoEditar.value && compraEditar.value != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoEditar.value = false
            },
            title = {
                Text("Editar compra")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = proveedorEditar.value,
                        onValueChange = { proveedorEditar.value = it },
                        label = { Text("Proveedor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cantidadEditar.value,
                        onValueChange = { cantidadEditar.value = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = precioEditar.value,
                        onValueChange = { precioEditar.value = it },
                        label = { Text("Precio compra") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notaEditar.value,
                        onValueChange = { notaEditar.value = it },
                        label = { Text("Nota") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = compraEditar.value?.id
                        val cantidad = cantidadEditar.value.toDoubleOrNull() ?: 0.0
                        val precio = precioEditar.value.toLongOrNull() ?: 0L

                        if (id != null) {
                            vm.actualizarCompra(
                                id = id,
                                proveedor = proveedorEditar.value,
                                cantidad = cantidad,
                                precio = precio,
                                nota = notaEditar.value
                            )
                            mostrarDialogoEditar.value = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoEditar.value = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}