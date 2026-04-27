package com.ethandev.cafecontable.ui.screen.cuentasporpagar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.text.input.KeyboardType
import com.ethandev.cafecontable.domain.constants.EstadoCuentaPorPagar

@Composable
fun CuentasPorPagarScreen(vm: CuentasPorPagarViewModel) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var cuentaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var cuentaHistorialId by remember { mutableStateOf<String?>(null) }
    var valorAbonoTxt by remember { mutableStateOf("") }
    var notaAbonoTxt by remember { mutableStateOf("") }


    var tabSeleccionado by remember { mutableStateOf(0) }

    val tabs = listOf("Pendientes", "Canceladas", "Todas")

    val cuentasFiltradas = when (tabSeleccionado) {
        0 -> state.items.filter { it.estado == EstadoCuentaPorPagar.PENDIENTE }
        1 -> state.items.filter { it.estado == EstadoCuentaPorPagar.CANCELADO }
        else -> state.items
    }



    LaunchedEffect(Unit) { vm.cargar() }

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.padding(6.dp))
            }

            if (!state.loading && state.items.isEmpty()) {
                Text("No hay cuentas por pagar pendientes.")
            }



            TabRow(selectedTabIndex = tabSeleccionado) {
                tabs.forEachIndexed { index, titulo ->
                    Tab(
                        selected = tabSeleccionado == index,
                        onClick = { tabSeleccionado = index },
                        text = { Text(titulo) }
                    )
                }
            }

            Spacer(modifier = Modifier.padding(6.dp))

            if (!state.loading && cuentasFiltradas.isEmpty()) {
                Text(
                    text = when (tabSeleccionado) {
                        0 -> "No hay cuentas por pagar pendientes."
                        1 -> "No hay cuentas canceladas."
                        else -> "No hay cuentas por pagar registradas."
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cuentasFiltradas) { item ->
                    val fecha = remember(item.fecha) {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(item.fecha))
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.proveedor,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Fecha: $fecha")
                            Text("Valor inicial: ${item.valorInicial} COP")
                            Text("Saldo pendiente: ${item.saldoPendiente} COP")
                            Text("Estado: ${item.estado}")
                            if (!item.nota.isNullOrBlank()) {
                                Text("Nota: ${item.nota}")
                            }

                            Spacer(modifier = Modifier.padding(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (item.estado == EstadoCuentaPorPagar.PENDIENTE) {
                                    Button(
                                        onClick = {
                                            cuentaSeleccionadaId = item.id
                                            valorAbonoTxt = item.saldoPendiente.toString()
                                            notaAbonoTxt = ""
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Registrar abono")
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        cuentaHistorialId = item.id
                                        vm.cargarAbonos(item.id)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Ver abonos")
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (cuentaSeleccionadaId != null) {

        AlertDialog(
            onDismissRequest = { cuentaSeleccionadaId = null },
            title = { Text("Registrar abono") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = valorAbonoTxt,
                        onValueChange = { valorAbonoTxt = it.filter(Char::isDigit) },
                        label = { Text("Valor abonado") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )


                    OutlinedTextField(
                        value = notaAbonoTxt,
                        onValueChange = { notaAbonoTxt = it },
                        label = { Text("Nota (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valor = valorAbonoTxt.toLongOrNull() ?: 0L
                        val cuentaId = cuentaSeleccionadaId
                        if (cuentaId != null && valor > 0) {
                            vm.registrarAbono(
                                cuentaId = cuentaId,
                                valor = valor,
                                nota = notaAbonoTxt.ifBlank { null }
                            )
                            cuentaSeleccionadaId = null
                        }
                    }
                ) {
                    Text("Guardar abono")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { cuentaSeleccionadaId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (cuentaHistorialId != null) {
        AlertDialog(
            onDismissRequest = {
                cuentaHistorialId = null
                vm.limpiarAbonos()
            },
            title = { Text("Historial de abonos") },
            text = {
                if (state.abonos.isEmpty()) {
                    Text("No hay abonos registrados.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.abonos) { abono ->
                            val fecha = remember(abono.fecha) {
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .format(Date(abono.fecha))
                            }

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Fecha: $fecha")
                                    Text("Valor: ${abono.valor} COP")
                                    if (!abono.nota.isNullOrBlank()) {
                                        Text("Nota: ${abono.nota}")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        cuentaHistorialId = null
                        vm.limpiarAbonos()
                    }
                ) {
                    Text("Cerrar")
                }
            },
            dismissButton = {}
        )
    }
}