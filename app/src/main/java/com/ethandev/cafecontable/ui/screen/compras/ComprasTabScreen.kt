package com.ethandev.cafecontable.ui.screen.compras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasScreen
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel

@Composable
fun ComprasTabScreen(
    compraVm: CompraCafeViewModel,
    historialVm: HistorialComprasViewModel
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F5EF))) {
        TabRow(
            selectedTabIndex = tabSeleccionado,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            containerColor = Color.White,
            contentColor = Color(0xFF376B4B),
            divider = {}
        ) {
            Tab(
                selected = tabSeleccionado == 0,
                onClick = { tabSeleccionado = 0 },
                text = {
                    Text("Nueva compra", fontWeight = if (tabSeleccionado == 0) FontWeight.Bold else FontWeight.Normal)
                }
            )
            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 },
                text = {
                    Text("Historial", fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Normal)
                }
            )
        }
        when (tabSeleccionado) {
            0 -> CompraCafeScreen(vm = compraVm)
            1 -> HistorialComprasScreen(vm = historialVm)
        }
    }
}
