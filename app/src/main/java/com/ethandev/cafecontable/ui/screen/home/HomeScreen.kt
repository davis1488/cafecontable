

package com.ethandev.cafecontable.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onGoHistorialCompras: () -> Unit,
    onGoHistorialVentas: () -> Unit,
    onGoCuentasPorCobrar: () -> Unit,
    onGoCuentasPorPagar: () -> Unit,
    onGoPrestamos: () -> Unit

) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            //.padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Café Contable",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gestiona compras, ventas, inventario e historiales de forma simple.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SectionTitle("Operaciones")
        }

        item {
            HomeMenuCard(
                title = "Registrar compra",
                subtitle = "Ingresa café comprado y actualiza inventario",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                buttonText = "Abrir",
                onClick = onGoCompras
            )
        }

        item {
            HomeMenuCard(
                title = "Registrar venta",
                subtitle = "Registra ventas y descuenta existencias",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PointOfSale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                buttonText = "Abrir",
                onClick = onGoVentas
            )
        }

        item {
            HomeMenuCard(
                title = "Registrar Anuncio",
                subtitle = "Registra Anuncioas por entregar",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PointOfSale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                buttonText = "Abrir",
                onClick = onGoVentasPedido
            )
        }

        item {
            HomeMenuCard(
                title = "Mezclas",
                subtitle = "Registra Mezcla para Entregas",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PointOfSale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                buttonText = "Abrir",
                onClick = onGoMezclas
            )
        }

        item {
            HomeMenuCard(
                title = "Asignacion de Mezcla",
                subtitle = "Asigna Mezcla a Venta",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PointOfSale,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                buttonText = "Abrir",
                onClick = onGoAsignacionMezclaPedido
            )
        }

        item {
            HomeMenuCard(
                title = "Ver inventario",
                subtitle = "Consulta existencias actuales por producto",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                buttonText = "Ver",
                onClick = onGoInventario
            )
        }

        item {
            SectionTitle("Historial")
        }

        item {
            HomeMenuCard(
                title = "Historial de compras",
                subtitle = "Consulta compras registradas",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                buttonText = "Ver",
                onClick = onGoHistorialCompras
            )
        }

        item {
            HomeMenuCard(
                title = "Historial de ventas",
                subtitle = "Consulta ventas registradas",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                buttonText = "Ver",
                onClick = onGoHistorialVentas
            )
        }
        item {
            HomeMenuCard(
                title = "Cuentas por cobrar",
                subtitle = "Consulta ventas a crédito pendientes",
                icon = { Icon(Icons.Filled.History, contentDescription = null) },
                buttonText = "Ver",
                onClick = onGoCuentasPorCobrar
            )
        }

        item {
            HomeMenuCard(
                title = "Cuentas por pagar",
                subtitle = "Consulta ventas a crédito pendientes",
                icon = { Icon(Icons.Filled.History, contentDescription = null) },
                buttonText = "Ver",
                onClick = onGoCuentasPorPagar
            )
        }

        item {
            HomeMenuCard(
                title = "Prestamos",
                subtitle = "Consulta Prestamos",
                icon = { Icon(Icons.Filled.History, contentDescription = null) },
                buttonText = "Ver",
                onClick = onGoPrestamos
            )
        }



        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun HomeMenuCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            icon()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}