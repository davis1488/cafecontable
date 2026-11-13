
package com.ethandev.cafecontable.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val cafeOscuro = Color(0xFF493628)
    val cafeClaro = Color(0xFF6B5240)
    val dorado = Color(0xFFF4D9AF)


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = cafeOscuro,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            cafeClaro,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )

                Text(
                    text = "Gestión de café",
                    style = MaterialTheme.typography.labelSmall,
                    color = dorado
                )
            }

            IconButton(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .size(42.dp)
                    .background(cafeClaro, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = "Cerrar sesión",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        cafeClaro,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalCafe,
                    contentDescription = null,
                    tint = dorado
                )
            }
        }
    }
}
