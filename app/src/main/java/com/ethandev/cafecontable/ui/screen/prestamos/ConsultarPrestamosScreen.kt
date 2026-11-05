package com.ethandev.cafecontable.ui.screen.prestamos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.model.RegistrarAbonoPrestamoInput
import kotlinx.coroutines.flow.flowOf
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val CafePrestamo = Color(0xFF493628)
private val VerdePrestamo = Color(0xFF376B4B)
private val FondoPrestamo = Color(0xFFF7F5EF)
private val TextoPrestamo = Color(0xFF29231E)
private val SecundarioPrestamo = Color(0xFF665C53)
private val RojoPrestamo = Color(0xFFC62828)
private val VerdeClaroPrestamo = Color(0xFFE7EEE7)

private fun dineroPrestamo(valor: Double): String =
    "$ " + NumberFormat.getNumberInstance(Locale("es", "CO"))
        .apply { maximumFractionDigits = 2; minimumFractionDigits = 0 }
        .format(valor)

private fun fechaAbonoPrestamo(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))
        .apply { timeZone = TimeZone.getTimeZone("America/Bogota") }
        .format(Date(timestamp))

@Composable
private fun FilaPrestamo(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, color = SecundarioPrestamo, style = MaterialTheme.typography.bodyMedium)
        Text(
            valor,
            color = if (destacado) VerdePrestamo else TextoPrestamo,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ConsultaPrestamosScreen(
    viewModel: PrestamosViewModel,
    paddingValues: PaddingValues
) {
    // paddingValues se conserva por compatibilidad con la navegación.
    // La barra superior del AppNavGraph ya aporta el espacio necesario.
    val prestamos by viewModel.prestamos.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val yaConsulto by viewModel.yaConsulto.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    var mostrarDialogoAbono by remember { mutableStateOf(false) }
    var mostrarDialogoHistorial by remember { mutableStateOf(false) }
    var prestamoSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var saldoPrestamoSeleccionado by remember { mutableStateOf(0.0) }
    var nombrePrestamoSeleccionado by remember { mutableStateOf("") }
    var valorAbono by remember { mutableStateOf("") }
    var observacionAbono by remember { mutableStateOf("") }
    var errorAbono by remember { mutableStateOf<String?>(null) }

    val flujoAbonos = remember(prestamoSeleccionadoId) {
        prestamoSeleccionadoId?.let { viewModel.listarAbonos(it) } ?: flowOf(emptyList())
    }
    val abonos by flujoAbonos.collectAsState(initial = emptyList())

    LaunchedEffect(toastMessage) {
        toastMessage?.let { mensaje ->
            viewModel.clearToastMessage()
            snackbar.showSnackbar(mensaje)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = FondoPrestamo,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CafePrestamo,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(color = CafePrestamo, shape = RoundedCornerShape(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFF4D9AF),
                            modifier = Modifier.size(34.dp)
                        )
                        Column {
                            Text(
                                "Consulta de préstamos", color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Saldos, movimientos y abonos",
                                color = Color(0xFFEBDAC8),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Buscar préstamos",
                            color = CafePrestamo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = busqueda,
                            onValueChange = viewModel::onBusquedaChange,
                            label = { Text("Nombre o cédula") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = CafePrestamo,
                                unfocusedLabelColor = TextoPrestamo,
                                focusedBorderColor = VerdePrestamo,
                                unfocusedBorderColor = Color(0xFF9B8D80),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = VerdePrestamo
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = viewModel::consultarPrestamos,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdePrestamo)
                            ) { Text("Consultar") }
                            OutlinedButton(
                                onClick = viewModel::consultarTodosLosPrestamos,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Ver todos", color = CafePrestamo) }
                        }
                        TextButton(
                            onClick = viewModel::limpiarConsulta,
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Limpiar búsqueda", color = CafePrestamo) }
                    }
                }
            }

            if (yaConsulto && prestamos.isNotEmpty()) {
                item {
                    val totalPrestado = prestamos.sumOf { it.valorPrestado }
                    val saldoActual = prestamos.sumOf { it.saldoPendiente }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VerdeClaroPrestamo),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Resumen de la consulta", color = CafePrestamo, fontWeight = FontWeight.Bold)
                            FilaPrestamo("Préstamos encontrados", prestamos.size.toString())
                            FilaPrestamo("Valor prestado", dineroPrestamo(totalPrestado))
                            HorizontalDivider(color = Color(0xFFD1DED0))
                            FilaPrestamo("Saldo pendiente", dineroPrestamo(saldoActual), destacado = true)
                        }
                    }
                }
            }

            if (!yaConsulto || prestamos.isEmpty()) {
                item {
                    Surface(color = Color.White, shape = RoundedCornerShape(20.dp)) {
                        Text(
                            text = if (!yaConsulto)
                                "Busca un préstamo por nombre o cédula, o selecciona Ver todos."
                            else "No se encontraron préstamos para esta consulta.",
                            modifier = Modifier.fillMaxWidth().padding(22.dp),
                            color = SecundarioPrestamo,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                items(prestamos, key = { it.id }) { prestamo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                prestamo.nombrePersona,
                                color = CafePrestamo,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Cédula: ${prestamo.cedulaPersona}", color = SecundarioPrestamo)
                            FilaPrestamo("Estado", prestamo.estado.toString())
                            FilaPrestamo("Valor prestado", dineroPrestamo(prestamo.valorPrestado))
                            Surface(color = VerdeClaroPrestamo, shape = RoundedCornerShape(14.dp)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text("Saldo pendiente", color = CafePrestamo, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        dineroPrestamo(prestamo.saldoPendiente),
                                        color = VerdePrestamo,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        prestamoSeleccionadoId = prestamo.id
                                        saldoPrestamoSeleccionado = prestamo.saldoPendiente
                                        nombrePrestamoSeleccionado = prestamo.nombrePersona
                                        valorAbono = ""
                                        observacionAbono = ""
                                        errorAbono = null
                                        mostrarDialogoAbono = true
                                    },
                                    enabled = prestamo.saldoPendiente > 0.0,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrestamo)
                                ) {
                                    Icon(Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Abonar")
                                }
                                OutlinedButton(
                                    onClick = {
                                        prestamoSeleccionadoId = prestamo.id
                                        nombrePrestamoSeleccionado = prestamo.nombrePersona
                                        mostrarDialogoHistorial = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.History, contentDescription = null, tint = CafePrestamo, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Abonos", color = CafePrestamo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAbono && prestamoSeleccionadoId != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAbono = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Registrar abono", color = CafePrestamo, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(nombrePrestamoSeleccionado, color = TextoPrestamo, fontWeight = FontWeight.Medium)
                    FilaPrestamo("Saldo actual", dineroPrestamo(saldoPrestamoSeleccionado), destacado = true)
                    OutlinedTextField(
                        value = valorAbono,
                        onValueChange = { valorAbono = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Valor del abono") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = CafePrestamo,
                            unfocusedLabelColor = TextoPrestamo,
                            focusedBorderColor = VerdePrestamo,
                            unfocusedBorderColor = Color(0xFF9B8D80)
                        )
                    )
                    OutlinedTextField(
                        value = observacionAbono,
                        onValueChange = { observacionAbono = it },
                        label = { Text("Observación (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = CafePrestamo,
                            unfocusedLabelColor = TextoPrestamo,
                            focusedBorderColor = VerdePrestamo,
                            unfocusedBorderColor = Color(0xFF9B8D80)
                        )
                    )
                    errorAbono?.let { Text(it, color = RojoPrestamo, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valor = valorAbono.toDoubleOrNull() ?: 0.0
                        when {
                            valor <= 0.0 -> errorAbono = "Ingresa un valor de abono válido."
                            valor > saldoPrestamoSeleccionado -> errorAbono = "El abono supera el saldo pendiente."
                            else -> {
                                val id = prestamoSeleccionadoId ?: return@Button
                                viewModel.registrarAbono(
                                    RegistrarAbonoPrestamoInput(
                                        prestamoId = id,
                                        fechaAbono = System.currentTimeMillis(),
                                        valorAbono = valor,
                                        observacion = observacionAbono
                                    )
                                )
                                mostrarDialogoAbono = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrestamo)
                ) { Text("Guardar abono") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAbono = false }) {
                    Text("Cancelar", color = RojoPrestamo)
                }
            }
        )
    }

    if (mostrarDialogoHistorial && prestamoSeleccionadoId != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoHistorial = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Abonos de $nombrePrestamoSeleccionado", color = CafePrestamo, fontWeight = FontWeight.Bold) },
            text = {
                if (abonos.isEmpty()) {
                    Text("Este préstamo no tiene abonos registrados.", color = SecundarioPrestamo)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    ) {
                        items(abonos) { abono ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = FondoPrestamo),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(fechaAbonoPrestamo(abono.fechaAbono), color = SecundarioPrestamo)
                                    Text(
                                        dineroPrestamo(abono.valorAbono),
                                        color = VerdePrestamo,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        abono.observacion.ifBlank { "Sin observación" },
                                        color = TextoPrestamo
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoHistorial = false }) {
                    Text("Cerrar", color = CafePrestamo)
                }
            }
        )
    }
}
