package com.ethandev.cafecontable.ui.screen.asignacionmezcla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ethandev.cafecontable.ui.screen.mezcla.MezclasScreen
import com.ethandev.cafecontable.ui.screen.mezcla.MezclasViewModel
import com.ethandev.cafecontable.ui.screen.ventaspedido.VentasPedidoScreen
import com.ethandev.cafecontable.ui.screen.ventaspedido.VentasPedidoViewModel

@Composable
fun MezclaAnuncioTabScreen(
    MezclasVm: MezclasViewModel,
    VentasPedidoVm: VentasPedidoViewModel,
    AsignacionMezclaPedidoVm: AsignacionMezclaPedidoViewModel,
    onIrAsignacionMezcla: (String) -> Unit
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val pestañas = listOf("Mezclas", "Anuncios", "Asignar")
    Column(Modifier.fillMaxSize().background(Color(0xFFF7F5EF))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                .background(Color.White, RoundedCornerShape(14.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            pestañas.forEachIndexed { indice, titulo ->
                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = { tabSeleccionado = indice },
                    shape = RoundedCornerShape(11.dp),
                    color = if (tabSeleccionado == indice) Color(0xFF376B4B) else Color.Transparent
                ) {
                    Box(Modifier.padding(vertical = 11.dp)) {
                        Text(
                            titulo,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (tabSeleccionado == indice) Color.White else Color(0xFF493628),
                            fontWeight = if (tabSeleccionado == indice) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        Box(Modifier.fillMaxSize()) {
            when (tabSeleccionado) {
                0 -> MezclasScreen(viewModel = MezclasVm)
                1 -> VentasPedidoScreen(vm = VentasPedidoVm, onIrAsignacionMezcla = onIrAsignacionMezcla)
                2 -> AsignacionMezclaPedidoScreen(viewModel = AsignacionMezclaPedidoVm)
            }
        }
    }
}
