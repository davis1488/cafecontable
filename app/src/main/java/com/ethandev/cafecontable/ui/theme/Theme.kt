
package com.ethandev.cafecontable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CafeColorScheme = lightColorScheme(

    // Colores principales
    primary = VerdeCafe,
    onPrimary = SuperficieCafe,

    primaryContainer = VerdeSuave,
    onPrimaryContainer = CafePrincipal,

    // Colores secundarios
    secondary = CafePrincipal,
    onSecondary = SuperficieCafe,

    secondaryContainer = DoradoCafe,
    onSecondaryContainer = CafePrincipal,

    // Colores terciarios
    tertiary = CafeMedio,
    onTertiary = SuperficieCafe,

    // Fondo general
    background = FondoCafe,
    onBackground = TextoPrincipal,

    // Tarjetas y formularios
    surface = SuperficieCafe,
    onSurface = TextoPrincipal,

    surfaceVariant = FondoCafe,
    onSurfaceVariant = TextoSecundario,

    surfaceContainer = SuperficieCafe,
    surfaceContainerLow = FondoCafe,
    surfaceContainerHigh = VerdeSuave,

    // Bordes
    outline = BordeCafe,
    outlineVariant = BordeCafe.copy(alpha = 0.6f),

    // Errores
    error = RojoError,
    onError = SuperficieCafe,

    // Colores inversos
    inverseSurface = CafePrincipal,
    inverseOnSurface = SuperficieCafe,
    inversePrimary = DoradoCafe
)

@Composable
fun CafecontableTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CafeColorScheme,
        typography = Typography,
        content = content
    )
}
