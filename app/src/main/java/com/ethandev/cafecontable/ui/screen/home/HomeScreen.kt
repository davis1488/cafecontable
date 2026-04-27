package com.ethandev.cafecontable.ui.screen.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    onGoCompras: () -> Unit,
    onGoVentas: () -> Unit,
    onGoVentasPedido: () -> Unit,
    onGoInventario: () -> Unit,
    onGoMezclas: () -> Unit,
    onGoAsignacionMezclaPedido: () -> Unit,
   // onGoHistorialCompras: () -> Unit,
    onGoHistorialVentas: () -> Unit,
    onGoCuentasPorCobrar: () -> Unit,
    onGoCuentasPorPagar: () -> Unit,
    onGoPrestamos: () -> Unit,
    onGoLiquidacion: () -> Unit,
    onGoOperacionesGastos: () -> Unit,
    onGoUtilidad: () -> Unit

) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HomeHeader()
        }

        item {
            SectionTitle("Operaciones principales")
        }

        item {
            HomeMenuCard(
                title = "Registrar compra",
                subtitle = "Ingresa compras de café e Historial",
                icon = Icons.Filled.ShoppingCart,
                onClick = onGoCompras
            )
        }

        ///////////////////////PENDINTE VENTAS////////////////////////

//        item {
//            HomeMenuCard(
//                title = "Registrar venta",
//                subtitle = "Guarda ventas directas y descuenta inventario",
//                icon = Icons.Filled.PointOfSale,
//                onClick = onGoVentas
//            )
//        }


        ///////////////////////Anuncios y Mezclas/////////////////////////


        item {
            SectionTitle("Anuncios y Entregas")
        }

        item {
            HomeMenuCard(
                title = "Registrar anuncio",
                subtitle = "Crea pedidos o compromisos de venta",
                icon = Icons.Filled.ReceiptLong,
                onClick = onGoVentasPedido
            )
        }

        item {
            HomeMenuCard(
                title = "Registrar mezclas",
                subtitle = "Prepara mezclas para entrega o análisis",
                icon = Icons.Filled.LocalCafe,
                onClick = onGoMezclas
            )
        }

        item {
            HomeMenuCard(
                title = "Asignar mezcla a anuncio",
                subtitle = "Relaciona mezcla disponible con pedidos",
                icon = Icons.Filled.AutoGraph,
                onClick = onGoAsignacionMezclaPedido
            )
        }
////////////////////////////////
        item {
            SectionTitle("Liquidaciones y Operaciones")
        }

        item {
            HomeMenuCard(
                title = "Liquidaciones",
                subtitle = "Consulta y procesa entregas pendientes por liquidar",
                icon = Icons.Filled.AccountBalanceWallet,
                onClick = onGoLiquidacion
            )
        }

        item {
            HomeMenuCard(
                title = "Operaciones y Gastos",
                subtitle = "Creacion de operaciones y gastos",
                icon = Icons.Filled.AccountBalanceWallet,
                onClick = onGoOperacionesGastos
            )
        }


        item {
            SectionTitle("Inventarios y Utilidad")
        }
        item {
            HomeMenuCard(
                title = "Inventario",
                subtitle = "Consulta existencias actuales y movimientos",
                icon = Icons.Filled.Inventory2,
                onClick = onGoInventario
            )
        }

        item {
            HomeMenuCard(
                title = "Utilidades",
                subtitle = "Calcula ganancias y resultados de operaciones",
                icon = Icons.Filled.AttachMoney,
                onClick = onGoUtilidad
            )
        }



        item {
            SectionTitle("Creditos y Movimientos")
        }


        ////////////////////////////////////////////////

//        item {
//            HomeMenuCard(
//                title = "Historial de compras",
//                subtitle = "Revisa todas las compras registradas",
//                icon = Icons.Filled.History,
//                onClick = onGoHistorialCompras
//            )
//        }
///////////////////////////////////////PENDIENTE VENTAS
        //item {
//            HomeMenuCard(
//                title = "Cuentas por COBRAR",
//                subtitle = "Consulta saldos pendientes de clientes",
//                icon = Icons.Filled.AccountBalanceWallet,
//                onClick = onGoCuentasPorCobrar
//            )
//        }

//////////////////////////////////////////////////////////////
        item {
            HomeMenuCard(
                title = "Cuentas por PAGAR",
                subtitle = "Consulta obligaciones y compras a crédito",
                icon = Icons.Filled.AccountBalanceWallet,
                onClick = onGoCuentasPorPagar
            )
        }

        item {
            HomeMenuCard(
                title = "Préstamos",
                subtitle = "Consulta préstamos y sus movimientos",
                icon = Icons.Filled.ReceiptLong,
                onClick = onGoPrestamos
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalCafe,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Text(
                text = "Café Contable",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Gestiona compras, anuncios, mezclas, inventario y liquidaciones desde un solo lugar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun HomeMenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onClick) {
                Text("Abrir")
            }
        }
    }
}