package com.ethandev.cafecontable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.ui.screen.productos.ProductosScreen
import com.ethandev.cafecontable.ui.screen.productos.ProductosViewModel
import com.ethandev.cafecontable.ui.theme.CafecontableTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Crear dependencias
        val (crearProductoUseCase, listarProductosUseCase) =
            AppModule.provideProductoUseCases(this)

        // Crear ViewModel
        val viewModel = ProductosViewModel(
            crearProductoUseCase,
            listarProductosUseCase
        )

        // Cargar UI
        setContent {

            CafecontableTheme {

                Surface {

                    ProductosScreen(viewModel)

                }

            }
        }
    }
}