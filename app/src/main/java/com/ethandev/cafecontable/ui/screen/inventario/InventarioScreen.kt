package com.ethandev.cafecontable.ui.screen.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InventarioScreen(vm: InventarioViewModel) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.cargar() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Inventario", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(state.items) { it ->
                ListItem(
                    headlineContent = { Text(it.nombre) },
                    supportingContent = { Text("${it.existencia} ${it.unidad}") }
                )
                Divider()
            }
        }
    }
}