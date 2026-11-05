package com.ethandev.cafecontable.ui.screen.prestamos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ethandev.cafecontable.domain.model.RegistrarPrestamoInput
import com.ethandev.cafecontable.ui.navigation.Routes
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

private val CafeOscuro = Color(0xFF493628)
private val VerdeCafePrestamo = Color(0xFF376B4B)
private val FondoPrestamo = Color(0xFFF7F5EF)
private val TextoPrestamo = Color(0xFF222222)
private val SecundarioPrestamo = Color(0xFF62564C)
private val BordePrestamo = Color(0xFF98897C)

private fun pesosPrestamo(valor: Double): String =
    "$" + NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }.format(valor)

@Composable
fun PrestamosScreen(
    navController: NavController,
    viewModel: PrestamosViewModel,
    paddingValues: PaddingValues
) {
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var interes by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }

    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { mensaje ->
            viewModel.clearToastMessage()
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    val valorNumero = valor.replace(',', '.').toDoubleOrNull() ?: 0.0
    val interesNumero = interes.replace(',', '.').toDoubleOrNull() ?: 0.0

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextoPrestamo,
        unfocusedTextColor = TextoPrestamo,
        focusedLabelColor = CafeOscuro,
        unfocusedLabelColor = TextoPrestamo,
        focusedPlaceholderColor = SecundarioPrestamo,
        unfocusedPlaceholderColor = SecundarioPrestamo,
        focusedBorderColor = VerdeCafePrestamo,
        unfocusedBorderColor = BordePrestamo,
        cursorColor = VerdeCafePrestamo,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = FondoPrestamo,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FondoPrestamo)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CafeOscuro)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFFF4D9AF)
                    )
                    Column {
                        Text(
                            "Préstamos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Registra y consulta el dinero prestado",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEBDAC8)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = VerdeCafePrestamo)
                        Spacer(Modifier.padding(horizontal = 5.dp))
                        Text(
                            "Nuevo préstamo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CafeOscuro
                        )
                    }
                    Text(
                        "Datos del beneficiario",
                        color = SecundarioPrestamo,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de la persona") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = coloresCampo
                    )
                    OutlinedTextField(
                        value = cedula,
                        onValueChange = { cedula = it },
                        label = { Text("Cédula") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = coloresCampo
                    )
                    Text(
                        "Condiciones del préstamo",
                        color = CafeOscuro,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text("Valor prestado (COP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = coloresCampo
                    )
                    OutlinedTextField(
                        value = interes,
                        onValueChange = { interes = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text("Interés") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Introduce el interés según el formato usado por tu aplicación") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = coloresCampo
                    )
                    OutlinedTextField(
                        value = observacion,
                        onValueChange = { observacion = it },
                        label = { Text("Observación (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = coloresCampo
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EEE7))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Valor del préstamo", color = CafeOscuro, style = MaterialTheme.typography.bodySmall)
                    Text(
                        pesosPrestamo(valorNumero),
                        color = VerdeCafePrestamo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (interes.isNotBlank()) {
                        Text("Interés ingresado: $interes", color = SecundarioPrestamo,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    val valorValido = valor.replace(',', '.').toDoubleOrNull()
                    val interesValido = interes.replace(',', '.').toDoubleOrNull()
                    val mensaje = when {
                        nombre.isBlank() -> "Ingrese el nombre de la persona"
                        cedula.isBlank() -> "Ingrese la cédula"
                        valorValido == null || !valorValido.isFinite() || valorValido <= 0.0 ->
                            "Ingrese un valor prestado válido"
                        interes.isNotBlank() && (interesValido == null || !interesValido.isFinite() || interesValido < 0.0) ->
                            "Ingrese un interés válido"
                        else -> null
                    }
                    if (mensaje != null) {
                        scope.launch { snackbarHostState.showSnackbar(mensaje) }
                    } else {
                        viewModel.registrarPrestamo(
                            RegistrarPrestamoInput(
                                nombrePersona = nombre.trim(),
                                cedulaPersona = cedula.trim(),
                                fechaPrestamo = System.currentTimeMillis(),
                                valorPrestado = valorValido!!,
                                interes = interesValido ?: 0.0,
                                observacion = observacion
                            )
                        )
                        // Mantiene el comportamiento original del formulario tras enviar.
                        nombre = ""
                        cedula = ""
                        valor = ""
                        interes = ""
                        observacion = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeCafePrestamo)
            ) {
                Text("Guardar préstamo", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { navController.navigate(Routes.CONSULTA_PRESTAMOS) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeOscuro)
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Consultar préstamos", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.padding(horizontal = 3.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
