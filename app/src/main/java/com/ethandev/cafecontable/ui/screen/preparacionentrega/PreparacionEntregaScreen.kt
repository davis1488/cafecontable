package com.ethandev.cafecontable.ui.screen.preparacionentrega

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PreparacionEntregaScreen(
    ventaId: String,
    vm: PreparacionEntregaViewModel
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var cafeTxt by remember { mutableStateOf("") }
    var pasillaTxt by remember { mutableStateOf("") }
    var regularTxt by remember { mutableStateOf("") }
    var notaTxt by remember { mutableStateOf("") }

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

    val cafe = cafeTxt.toDoubleOrNull() ?: 0.0
    val pasilla = pasillaTxt.toDoubleOrNull() ?: 0.0
    val regular = regularTxt.toDoubleOrNull() ?: 0.0
    val total = cafe + pasilla + regular

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Preparación de entrega",
                style = MaterialTheme.typography.headlineSmall
            )

            Text("Venta: $ventaId")

            OutlinedTextField(
                value = cafeTxt,
                onValueChange = { cafeTxt = it.replace(',', '.') },
                label = { Text("Cantidad Café") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pasillaTxt,
                onValueChange = { pasillaTxt = it.replace(',', '.') },
                label = { Text("Cantidad Pasilla") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = regularTxt,
                onValueChange = { regularTxt = it.replace(',', '.') },
                label = { Text("Cantidad Regular") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notaTxt,
                onValueChange = { notaTxt = it },
                label = { Text("Nota") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total preparado: $total KG",
                style = MaterialTheme.typography.titleMedium
            )

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(
                onClick = {
                    vm.registrar(
                        ventaId = ventaId,
                        cantidadCafe = cafe,
                        cantidadPasilla = pasilla,
                        cantidadRegular = regular,
                        nota = notaTxt.ifBlank { null },
                        factor = 0.0,
                        precioVentaUnitario = 0

                    )

                    cafeTxt = ""
                    pasillaTxt = ""
                    regularTxt = ""
                    notaTxt = ""
                },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.loading) "Guardando..." else "Registrar preparación")
            }
        }
    }
}