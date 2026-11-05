package com.ethandev.cafecontable.ui.screen.compras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasScreen
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel

private val FondoCompras = Color(0xFFF7F5EF)
private val CafeCompras = Color(0xFF493628)
private val VerdeCompras = Color(0xFF376B4B)

@Composable
fun ComprasTabScreen(
    compraVm: CompraCafeViewModel,
    historialVm: HistorialComprasViewModel
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().background(FondoCompras)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp)
                .background(Color.White, RoundedCornerShape(13.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Nueva compra", "Historial").forEachIndexed { indice, etiqueta ->
                val activo = tabSeleccionado == indice
                Surface(
                    onClick = { tabSeleccionado = indice },
                    modifier = Modifier.weight(1f),
                    color = if (activo) VerdeCompras else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        etiqueta,
                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                        color = if (activo) Color.White else CafeCompras,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Box(Modifier.weight(1f)) {
            when (tabSeleccionado) {
                0 -> CompraCafeScreen(vm = compraVm)
                1 -> HistorialComprasScreen(vm = historialVm)
            }
        }
    }
}
