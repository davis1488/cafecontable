package com.ethandev.cafecontable.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val CafeOscuro = Color(0xFF493628)
private val CafeMedio = Color(0xFF69503D)
private val VerdeCafe = Color(0xFF356B4B)
private val FondoCafe = Color(0xFFF7F5EF)
private val TextoCafe = Color(0xFF352A23)
private val TextoSecundario = Color(0xFF776C62)

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onGoCompras: () -> Unit,
    onGoVentas: () -> Unit,
    onGoVentasPedido: () -> Unit,
    onGoInventario: () -> Unit,
    onGoMezclas: () -> Unit,
    onGoAsignacionMezclaPedido: () -> Unit,
    onGoHistorialVentas: () -> Unit,
    onGoCuentasPorCobrar: () -> Unit,
    onGoCuentasPorPagar: () -> Unit,
    onGoPrestamos: () -> Unit,
    onGoLiquidacion: () -> Unit,
    onGoOperacionesGastos: () -> Unit,
    onGoUtilidad: () -> Unit
) {
    // Se conserva la misma firma de navegación que usa AppNavGraph.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCafe)
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HomeHeaderModerno() }

        item { TituloSeccion("Operaciones principales", "Registra y administra tu café") }
        item {
            FilaMenu(
                primero = MenuInicio("Compras", "Registro e historial", Icons.Filled.ShoppingCart, Color(0xFFE5F1E6), VerdeCafe, onGoCompras),
                segundo = MenuInicio("Inventario", "Existencias y movimientos", Icons.Filled.Inventory2, Color(0xFFF3EBDC), Color(0xFF92622C), onGoInventario)
            )
        }

        item { TituloSeccion("Anuncios y mezclas", "Del pedido a la preparación") }
        item {
            FilaMenu(
                primero = MenuInicio("Anuncios", "Pedidos de venta", Icons.Filled.ReceiptLong, Color(0xFFE7EDF7), Color(0xFF416696), onGoVentasPedido),
                segundo = MenuInicio("Mezclas", "Preparación y edición", Icons.Filled.LocalCafe, Color(0xFFF2E8DD), CafeMedio, onGoMezclas)
            )
        }
        item {
            FilaMenu(
                primero = MenuInicio("Asignaciones", "Mezclas a pedidos", Icons.Filled.AutoGraph, Color(0xFFECE9F5), Color(0xFF6E599A), onGoAsignacionMezclaPedido),
                segundo = null
            )
        }

        item { TituloSeccion("Finanzas", "Controla los resultados") }
        item {
            FilaMenu(
                primero = MenuInicio("Liquidaciones", "Entregas y descuentos", Icons.Filled.AccountBalanceWallet, Color(0xFFE3F1ED), Color(0xFF30776C), onGoLiquidacion),
                segundo = MenuInicio("Operaciones", "Operaciones y gastos", Icons.Filled.ReceiptLong, Color(0xFFF3E8E5), Color(0xFFAB6256), onGoOperacionesGastos)
            )
        }
        item {
            FilaMenu(
                primero = MenuInicio("Utilidades", "Ganancias por operación", Icons.Filled.AttachMoney, Color(0xFFE5F1E6), VerdeCafe, onGoUtilidad),
                segundo = MenuInicio("Cuentas por pagar", "Obligaciones y créditos", Icons.Filled.AccountBalanceWallet, Color(0xFFF3EBDC), Color(0xFF92622C), onGoCuentasPorPagar)
            )
        }
        item {
            FilaMenu(
                primero = MenuInicio("Préstamos", "Saldos y movimientos", Icons.Filled.ReceiptLong, Color(0xFFE7EDF7), Color(0xFF416696), onGoPrestamos),
                segundo = null
            )
        }
        // Se mantienen los callbacks de Ventas, Historial de Ventas y Cuentas por Cobrar
        // en la firma para no romper AppNavGraph; no se muestran porque estaban ocultos.
    }
}

private data class MenuInicio(
    val titulo: String,
    val subtitulo: String,
    val icono: ImageVector,
    val colorFondoIcono: Color,
    val colorIcono: Color,
    val onClick: () -> Unit
)

@Composable
private fun HomeHeaderModerno() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CafeOscuro),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(CafeMedio, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocalCafe, contentDescription = null, tint = Color(0xFFF7E3BF), modifier = Modifier.size(30.dp))
                }
                Column {
                    Text("CafeContable", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Gestión inteligente de tu café", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEADCCB))
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("Tu negocio, bajo control", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(
                "Compras, mezclas, inventario, liquidaciones y utilidades en un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF0E4D4)
            )
        }
    }
}

@Composable
private fun TituloSeccion(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titulo, color = TextoCafe, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitulo, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FilaMenu(primero: MenuInicio, segundo: MenuInicio?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TarjetaMenu(primero, Modifier.weight(1f))
        if (segundo != null) {
            TarjetaMenu(segundo, Modifier.weight(1f))
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TarjetaMenu(menu: MenuInicio, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(158.dp)
            .clickable(onClick = menu.onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(43.dp).background(menu.colorFondoIcono, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(menu.icono, contentDescription = null, tint = menu.colorIcono)
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    menu.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextoCafe,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    menu.subtitulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = menu.colorIcono, modifier = Modifier.size(17.dp))
            }
        }
    }
}
