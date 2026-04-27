package com.ethandev.cafecontable.ui.screen.compras

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasScreen
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel

@Composable
fun ComprasTabScreen(
    compraVm: CompraCafeViewModel,
    historialVm: HistorialComprasViewModel
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = tabSeleccionado) {
            Tab(
                selected = tabSeleccionado == 0,
                onClick = { tabSeleccionado = 0 },
                text = { Text("Comprar") }
            )

            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 },
                text = { Text("Historial") }
            )
        }

        when (tabSeleccionado) {
            0 -> CompraCafeScreen(vm = compraVm)
            1 -> HistorialComprasScreen(vm = historialVm)
        }
    }
}