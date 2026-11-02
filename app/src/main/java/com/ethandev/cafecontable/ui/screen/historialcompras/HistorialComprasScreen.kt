package com.ethandev.cafecontable.ui.screen.historialcompras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.domain.model.CompraHistorialItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val FondoCompras = Color(0xFFF7F5EF)
private val CafeOscuro = Color(0xFF493628)
private val VerdeCafe = Color(0xFF376B4B)
private val TextoSuave = Color(0xFF786D63)

private fun monedaHistorial(valor: Long): String =
    "$" + NumberFormat.getIntegerInstance(Locale("es", "CO")).format(valor)

private fun cantidadHistorial(valor: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 2
    }.format(valor)

@Composable
fun HistorialComprasScreen(vm: HistorialComprasViewModel) {
    val state by vm.state.collectAsState()
    val toastMessage by vm.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var mostrarEditar by remember { mutableStateOf(false) }
    var compraEditar by remember { mutableStateOf<CompraHistorialItem?>(null) }
    var proveedorEditar by remember { mutableStateOf("") }
    var cantidadEditar by remember { mutableStateOf("") }
    var precioEditar by remember { mutableStateOf("") }
    var notaEditar by remember { mutableStateOf("") }

    var mostrarAnular by remember { mutableStateOf(false) }
    var compraAnularId by remember { mutableStateOf<Int?>(null) }
    var compraAnularNombre by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.cargar() }
    LaunchedEffect(toastMessage) {
        toastMessage?.let { mensaje ->
            snackbarHostState.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
            vm.clearToastMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoCompras),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = CafeOscuro,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(color = Color(0xFF6B5240), shape = RoundedCornerShape(15.dp)) {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = null,
                                tint = Color(0xFFF4D9AF),
                                modifier = Modifier.padding(12.dp).size(26.dp)
                            )
                        }
                        Column {
                            Text("Historial de compras", color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Consulta y administra tus registros",
                                color = Color(0xFFEBDAC8),
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Compras registradas", color = CafeOscuro,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Surface(color = Color(0xFFE6EFE6), shape = RoundedCornerShape(50.dp)) {
                        Text(
                            "${state.items.size} registros",
                            color = VerdeCafe,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            state.error?.let { error ->
                item {
                    Surface(color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)) {
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp))
                    }
                }
            }

            if (state.loading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            if (!state.loading && state.items.isEmpty()) {
                item {
                    Surface(color = Color.White, shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = null,
                                tint = VerdeCafe, modifier = Modifier.size(34.dp))
                            Text("Todavía no hay compras registradas",
                                color = CafeOscuro, fontWeight = FontWeight.SemiBold)
                            Text("Las compras aparecerán aquí cuando las guardes.",
                                color = TextoSuave,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            items(state.items, key = { it.id }) { compra ->
                val fecha = remember(compra.fecha) {
                    SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault())
                        .format(Date(compra.fecha))
                }
                val anulada = compra.estado.equals("ANULADA", ignoreCase = true)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(color = Color(0xFFE6EFE6), shape = RoundedCornerShape(12.dp)) {
                                Icon(Icons.Filled.ReceiptLong, contentDescription = null,
                                    tint = VerdeCafe,
                                    modifier = Modifier.padding(10.dp).size(22.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(compra.productoNombre, color = CafeOscuro,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                Text(fecha, color = TextoSuave,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(
                                color = if (anulada) Color(0xFFFFE8E6) else Color(0xFFE6EFE6),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text(
                                    compra.estado,
                                    color = if (anulada) Color(0xFFAB453C) else VerdeCafe,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFEFEAE3))

                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cantidad", color = TextoSuave,
                                    style = MaterialTheme.typography.labelMedium)
                                Text("${cantidadHistorial(compra.cantidad)} ${compra.unidad}",
                                    color = CafeOscuro, fontWeight = FontWeight.SemiBold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Precio unitario", color = TextoSuave,
                                    style = MaterialTheme.typography.labelMedium)
                                Text(monedaHistorial(compra.precioUnitCompra),
                                    color = CafeOscuro, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(color = Color(0xFFEAF2E9), shape = RoundedCornerShape(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total de compra", color = VerdeCafe,
                                    style = MaterialTheme.typography.labelLarge)
                                Text(monedaHistorial(compra.total), color = VerdeCafe,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("Proveedor: ${compra.proveedor?.takeIf { it.isNotBlank() } ?: "Sin proveedor"}",
                            style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        Text(if (compra.esCredito) "Modalidad: A crédito" else "Modalidad: Contado",
                            style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        if (!compra.nota.isNullOrBlank()) {
                            Text("Nota: ${compra.nota}",
                                style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    compraEditar = compra
                                    proveedorEditar = compra.proveedor.orEmpty()
                                    cantidadEditar = compra.cantidad.toString()
                                    precioEditar = compra.precioUnitCompra.toString()
                                    notaEditar = compra.nota.orEmpty()
                                    mostrarEditar = true
                                },
                                enabled = !anulada,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeCafe),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null,
                                    modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(7.dp))
                                Text("Editar")
                            }
                            OutlinedButton(
                                onClick = {
                                    compraAnularId = compra.id
                                    compraAnularNombre = compra.productoNombre
                                    mostrarAnular = true
                                },
                                enabled = !anulada,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Anular")
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) { data ->
            val esExito = data.visuals.message.contains("correctamente", ignoreCase = true)
            val colorFondo = if (esExito) Color(0xFFE6F3E9) else Color(0xFFFFF0E2)
            val colorTexto = if (esExito) Color(0xFF205C37) else Color(0xFF92400E)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colorFondo,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (esExito) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = colorTexto,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (esExito) "Operación exitosa" else "No se pudo completar",
                            color = colorTexto,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = data.visuals.message,
                            color = colorTexto,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(
                        onClick = { data.dismiss() },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar mensaje", tint = colorTexto)
                    }
                }
            }
        }
    }

    if (mostrarAnular && compraAnularId != null) {
        AlertDialog(
            onDismissRequest = { mostrarAnular = false },
            title = { Text("Anular compra") },
            text = { Text("¿Deseas anular la compra de $compraAnularNombre?") },
            confirmButton = {
                TextButton(onClick = {
                    compraAnularId?.let(vm::anularCompra)
                    mostrarAnular = false
                }) { Text("Sí, anular") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarAnular = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarEditar && compraEditar != null) {
        AlertDialog(
            onDismissRequest = { mostrarEditar = false },
            title = { Text("Editar compra") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = proveedorEditar,
                        onValueChange = { proveedorEditar = it },
                        label = { Text("Proveedor") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cantidadEditar,
                        onValueChange = { cantidadEditar = it.replace(',', '.') },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = precioEditar,
                        onValueChange = { precioEditar = it.filter(Char::isDigit) },
                        label = { Text("Precio compra") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = notaEditar,
                        onValueChange = { notaEditar = it },
                        label = { Text("Nota") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = compraEditar?.id
                        val cantidad = cantidadEditar.toDoubleOrNull() ?: 0.0
                        val precio = precioEditar.toLongOrNull() ?: 0L
                        if (id != null && cantidad > 0 && precio > 0) {
                            vm.actualizarCompra(
                                id = id,
                                proveedor = proveedorEditar,
                                cantidad = cantidad,
                                precio = precio,
                                nota = notaEditar
                            )
                            mostrarEditar = false
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Ingresa cantidad y precio válidos",
                                    duration = SnackbarDuration.Long,
                                    withDismissAction = true
                                )
                            }
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEditar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
