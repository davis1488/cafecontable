package com.ethandev.cafecontable.ui.screen.asignacionmezcla


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = tabSeleccionado) {
            Tab(
                selected = tabSeleccionado == 0,
                onClick = { tabSeleccionado = 0 },
                text = { Text("Mezcla") }
            )

            Tab(
                selected = tabSeleccionado == 1,
                onClick = { tabSeleccionado = 1 },
                text = { Text("Anuncio") }
            )

            Tab(
                selected = tabSeleccionado == 2,
                onClick = { tabSeleccionado = 2 },
                text = { Text("Asignacion") }
            )
        }

        when (tabSeleccionado) {
            0 -> MezclasScreen(viewModel = MezclasVm)
            1 -> VentasPedidoScreen(vm = VentasPedidoVm)
            2 -> AsignacionMezclaPedidoScreen(viewModel = AsignacionMezclaPedidoVm)
        }
    }
}