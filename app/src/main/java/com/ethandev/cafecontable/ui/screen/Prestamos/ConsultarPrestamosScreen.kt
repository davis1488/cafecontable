package com.ethandev.cafecontable.ui.screen.prestamos

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPrestamoInput
import com.ethandev.cafecontable.ui.viewmodel.PrestamosViewModel

@Composable
fun ConsultaPrestamosScreen(
    viewModel: PrestamosViewModel,
    paddingValues: PaddingValues
) {
    val prestamos by viewModel.prestamos.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()

    val context = LocalContext.current
    val toastMessage by viewModel.toastMessage.collectAsState()

    var mostrarDialogoAbono by remember { mutableStateOf(false) }
    var prestamoSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var saldoPrestamoSeleccionado by remember { mutableStateOf(0.0) }
    var valorAbono by remember { mutableStateOf("") }
    var observacionAbono by remember { mutableStateOf("") }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Consultar préstamos")

        OutlinedTextField(
            value = busqueda,
            onValueChange = { viewModel.onBusquedaChange(it) },
            label = { Text("Buscar por nombre o cédula") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(prestamos) { prestamo ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Persona: ${prestamo.nombrePersona}")
                        Text("Cédula: ${prestamo.cedulaPersona}")
                        Text("Prestado: ${prestamo.valorPrestado}")
                        Text("Saldo: ${prestamo.saldoPendiente}")
                        Text("Estado: ${prestamo.estado}")

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    prestamoSeleccionadoId = prestamo.id
                                    saldoPrestamoSeleccionado = prestamo.saldoPendiente
                                    valorAbono = ""
                                    observacionAbono = ""
                                    mostrarDialogoAbono = true
                                }
                            ) {
                                Text("Abonar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAbono && prestamoSeleccionadoId != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoAbono = false
            },
            title = {
                Text("Registrar abono")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = valorAbono,
                        onValueChange = { valorAbono = it },
                        label = { Text("Valor abono") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = observacionAbono,
                        onValueChange = { observacionAbono = it },
                        label = { Text("Observación") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val valor = valorAbono.toDoubleOrNull() ?: 0.0

                        when {
                            valor <= 0.0 -> {
                                Toast.makeText(
                                    context,
                                    "Ingrese un valor de abono válido",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            valor > saldoPrestamoSeleccionado -> {
                                Toast.makeText(
                                    context,
                                    "El abono no puede ser mayor al saldo pendiente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            prestamoSeleccionadoId != null -> {
                                viewModel.registrarAbono(
                                    RegistrarAbonoPrestamoInput(
                                        prestamoId = prestamoSeleccionadoId!!,
                                        fechaAbono = System.currentTimeMillis(),
                                        valorAbono = valor,
                                        observacion = observacionAbono
                                    )
                                )
                                mostrarDialogoAbono = false
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoAbono = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}