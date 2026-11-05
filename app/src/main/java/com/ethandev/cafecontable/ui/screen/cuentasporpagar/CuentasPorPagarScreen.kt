package com.ethandev.cafecontable.ui.screen.cuentasporpagar

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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.constants.EstadoCuentaPorPagar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val CxpCafe = Color(0xFF493628)
private val CxpVerde = Color(0xFF376B4B)
private val CxpFondo = Color(0xFFF7F5EF)
private val CxpTexto = Color(0xFF29231E)
private val CxpSecundario = Color(0xFF665C53)
private val CxpBorde = Color(0xFF9B8D80)
private val CxpSuave = Color(0xFFE7EEE7)
private val CxpRojo = Color(0xFFC62828)

private fun cxpPesos(valor: Long): String =
    "$ " + NumberFormat.getNumberInstance(Locale("es", "CO")).format(valor)

private fun cxpFecha(fecha: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO")).apply {
        timeZone = TimeZone.getTimeZone("America/Bogota")
    }.format(Date(fecha))

@Composable
private fun CxpDato(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            color = CxpSecundario,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            color = if (destacado) CxpVerde else CxpTexto,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun CxpResumen(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(17.dp), color = Color.White) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = valor,
                color = CxpVerde,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = titulo, color = CxpSecundario, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CxpCampoColores() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = CxpTexto,
    unfocusedTextColor = CxpTexto,
    focusedLabelColor = CxpCafe,
    unfocusedLabelColor = CxpSecundario,
    focusedBorderColor = CxpVerde,
    unfocusedBorderColor = CxpBorde,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = CxpVerde
)

@Composable
fun CuentasPorPagarScreen(vm: CuentasPorPagarViewModel) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var cuentaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var cuentaHistorialId by remember { mutableStateOf<String?>(null) }
    var valorAbonoTxt by remember { mutableStateOf("") }
    var notaAbonoTxt by remember { mutableStateOf("") }
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    val pendientes = state.items.filter { it.estado == EstadoCuentaPorPagar.PENDIENTE }
    val canceladas = state.items.filter { it.estado == EstadoCuentaPorPagar.CANCELADO }
    val saldoPendienteTotal = pendientes.sumOf { it.saldoPendiente }
    val cuentasFiltradas = when (tabSeleccionado) {
        0 -> pendientes
        1 -> canceladas
        else -> state.items
    }
    val cuentaParaAbono = state.items.firstOrNull { it.id == cuentaSeleccionadaId }
    val valorAbono = valorAbonoTxt.toLongOrNull() ?: 0L
    val abonoValido = cuentaParaAbono != null && valorAbono > 0L &&
            valorAbono <= cuentaParaAbono.saldoPendiente && !state.loading

    LaunchedEffect(Unit) { vm.cargar() }
    LaunchedEffect(state.error, state.okMsg) {
        val error = state.error
        val mensaje = state.okMsg
        if (error != null || mensaje != null) {
            vm.limpiarMensajes()
            snackbarHostState.showSnackbar(error ?: mensaje.orEmpty())
        }
    }

    Scaffold(
        containerColor = CxpFondo,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (state.error != null) CxpRojo else CxpCafe,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(color = CxpCafe, shape = RoundedCornerShape(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFF4D9AF),
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "Cuentas por pagar",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Control de deudas y abonos a proveedores",
                                color = Color(0xFFEBDAC8),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item {
                Surface(color = CxpSuave, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Saldo total pendiente", color = CxpCafe, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            cxpPesos(saldoPendienteTotal),
                            color = CxpVerde,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CxpResumen("Pendientes", pendientes.size.toString(), Modifier.weight(1f))
                    CxpResumen("Canceladas", canceladas.size.toString(), Modifier.weight(1f))
                    CxpResumen("Todas", state.items.size.toString(), Modifier.weight(1f))
                }
            }
            item {
                TabRow(
                    selectedTabIndex = tabSeleccionado,
                    containerColor = Color.White,
                    contentColor = CxpVerde
                ) {
                    listOf("Pendientes", "Canceladas", "Todas").forEachIndexed { index, titulo ->
                        Tab(
                            selected = tabSeleccionado == index,
                            onClick = { tabSeleccionado = index },
                            text = {
                                Text(
                                    titulo,
                                    maxLines = 1,
                                    color = if (tabSeleccionado == index) CxpVerde else CxpSecundario
                                )
                            }
                        )
                    }
                }
            }
            if (state.loading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CxpVerde) }
            }
            if (!state.loading && cuentasFiltradas.isEmpty()) {
                item {
                    Surface(color = Color.White, shape = RoundedCornerShape(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = CxpVerde)
                            Text(
                                when (tabSeleccionado) {
                                    0 -> "No hay cuentas por pagar pendientes."
                                    1 -> "No hay cuentas canceladas."
                                    else -> "No hay cuentas por pagar registradas."
                                },
                                color = CxpSecundario
                            )
                        }
                    }
                }
            }
            items(cuentasFiltradas, key = { it.id }) { cuenta ->
                val pendiente = cuenta.estado == EstadoCuentaPorPagar.PENDIENTE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    cuenta.proveedor,
                                    color = CxpCafe,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(cxpFecha(cuenta.fecha), color = CxpSecundario,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(
                                color = if (pendiente) Color(0xFFFFF2DE) else CxpSuave,
                                shape = RoundedCornerShape(30.dp)
                            ) {
                                Text(
                                    if (pendiente) "PENDIENTE" else cuenta.estado.toString(),
                                    color = if (pendiente) Color(0xFF8A581D) else CxpVerde,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        CxpDato("Valor inicial", cxpPesos(cuenta.valorInicial))
                        Surface(color = CxpSuave, shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("Saldo pendiente", Modifier.weight(1f), color = CxpCafe)
                                Text(cxpPesos(cuenta.saldoPendiente),
                                    color = CxpVerde, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (!cuenta.nota.isNullOrBlank()) {
                            Text("Nota: ${cuenta.nota}", color = CxpSecundario,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        if (pendiente) {
                            Button(
                                onClick = {
                                    cuentaSeleccionadaId = cuenta.id
                                    valorAbonoTxt = cuenta.saldoPendiente.toString()
                                    notaAbonoTxt = ""
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CxpVerde),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Icon(Icons.Filled.Payments, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Registrar abono", color = Color.White)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                cuentaHistorialId = cuenta.id
                                vm.cargarAbonos(cuenta.id)
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CxpCafe),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Filled.History, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Ver historial de abonos")
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (cuentaSeleccionadaId != null) {
        AlertDialog(
            onDismissRequest = { if (!state.loading) cuentaSeleccionadaId = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Registrar abono", color = CxpCafe, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Saldo disponible para abonar: ${cxpPesos(cuentaParaAbono?.saldoPendiente ?: 0L)}",
                        color = CxpSecundario
                    )
                    OutlinedTextField(
                        value = valorAbonoTxt,
                        onValueChange = { valorAbonoTxt = it.filter(Char::isDigit) },
                        label = { Text("Valor abonado (COP)") },
                        colors = CxpCampoColores(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (valorAbono > (cuentaParaAbono?.saldoPendiente ?: 0L)) {
                        Text("El abono no puede superar el saldo pendiente.", color = CxpRojo,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(
                        value = notaAbonoTxt,
                        onValueChange = { notaAbonoTxt = it },
                        label = { Text("Nota (opcional)") },
                        colors = CxpCampoColores(),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = cuentaSeleccionadaId
                        if (id != null && abonoValido) {
                            vm.registrarAbono(
                                cuentaId = id,
                                valor = valorAbono,
                                nota = notaAbonoTxt.ifBlank { null }
                            )
                            cuentaSeleccionadaId = null
                        }
                    },
                    enabled = abonoValido,
                    colors = ButtonDefaults.buttonColors(containerColor = CxpVerde)
                ) { Text("Guardar abono") }
            },
            dismissButton = {
                TextButton(onClick = { cuentaSeleccionadaId = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = CxpRojo)) {
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
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Historial de abonos", color = CxpCafe, fontWeight = FontWeight.Bold) },
            text = {
                if (state.abonos.isEmpty()) {
                    Text("No hay abonos registrados.", color = CxpSecundario)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.abonos) { abono ->
                            Surface(color = CxpFondo, shape = RoundedCornerShape(13.dp)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(cxpFecha(abono.fecha), color = CxpSecundario,
                                        style = MaterialTheme.typography.bodySmall)
                                    Text(cxpPesos(abono.valor), color = CxpVerde,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                    if (!abono.nota.isNullOrBlank()) {
                                        Text(abono.nota.orEmpty(), color = CxpTexto)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cuentaHistorialId = null
                        vm.limpiarAbonos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CxpCafe)
                ) { Text("Cerrar") }
            }
        )
    }
}
