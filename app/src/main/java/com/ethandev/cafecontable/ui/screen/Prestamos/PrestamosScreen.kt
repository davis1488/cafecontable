package com.ethandev.cafecontable.ui.screen.prestamos

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ethandev.cafecontable.domain.model.RegistrarPrestamoInput
import com.ethandev.cafecontable.ui.navigation.Routes
import com.ethandev.cafecontable.ui.viewmodel.PrestamosViewModel

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

    val context = LocalContext.current
    val toastMessage by viewModel.toastMessage.collectAsState()

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
        Text("Registrar préstamo")

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre persona") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cedula,
            onValueChange = { cedula = it },
            label = { Text("Cédula") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor prestado") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = interes,
            onValueChange = { interes = it },
            label = { Text("Interés") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = observacion,
            onValueChange = { observacion = it },
            label = { Text("Observación") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val valorDouble = valor.toDoubleOrNull() ?: 0.0
                val interesDouble = interes.toDoubleOrNull() ?: 0.0

                if (nombre.isBlank()) {
                    Toast.makeText(context, "Ingrese el nombre", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (cedula.isBlank()) {
                    Toast.makeText(context, "Ingrese la cédula", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (valorDouble <= 0.0) {
                    Toast.makeText(context, "Ingrese un valor válido", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                viewModel.registrarPrestamo(
                    RegistrarPrestamoInput(
                        nombrePersona = nombre,
                        cedulaPersona = cedula,
                        fechaPrestamo = System.currentTimeMillis(),
                        valorPrestado = valorDouble,
                        interes = interesDouble,
                        observacion = observacion
                    )
                )

                nombre = ""
                cedula = ""
                valor = ""
                interes = ""
                observacion = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar préstamo")
        }


        Button(
            onClick = {
                navController.navigate(Routes.CONSULTA_PRESTAMOS)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Consultar préstamos")
        }
    }
}