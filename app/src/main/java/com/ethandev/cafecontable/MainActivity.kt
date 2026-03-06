//package com.ethandev.cafecontable
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import com.ethandev.cafecontable.core.AppModule
//import com.ethandev.cafecontable.ui.screen.productos.ProductosScreen
//import com.ethandev.cafecontable.ui.screen.productos.ProductosViewModel
//import com.ethandev.cafecontable.ui.theme.CafecontableTheme
//import com.ethandev.cafecontable.ui.screen.compras.CompraCafeScreen
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        super.onCreate(savedInstanceState)
//
//        enableEdgeToEdge()
//
//        // Crear dependencias
//        val (crearProductoUseCase, listarProductosUseCase) =
//            AppModule.provideProductoUseCases(this)
//
//        // Crear ViewModel
//        val viewModel = ProductosViewModel(
//            crearProductoUseCase,
//            listarProductosUseCase
//        )
//
//        // Cargar UI
////        setContent {
////
////            CafecontableTheme {
////
////                Surface {
////
////                    ProductosScreen(viewModel)
////
////                }
////
////            }
////        }
//        val registrarCompraUC = AppModule.provideCompraUseCase(this)
//        val compraVm = com.ethandev.cafecontable.ui.screen.compras.CompraCafeViewModel(registrarCompraUC)
//
//        setContent {
//            CafecontableTheme {
//                CompraCafeScreen(compraVm)
//            }
//        }
//    }
//}


////////////////////////////////////////////////////////
package com.ethandev.cafecontable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.ui.screen.inventario.InventarioScreen
import com.ethandev.cafecontable.ui.screen.inventario.InventarioViewModel
import com.ethandev.cafecontable.ui.theme.CafecontableTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val (_, _, listarInventarioUseCase) = AppModule.provideInventarioUseCases(this)

        val inventarioVm = InventarioViewModel(
            listarInventarioUseCase
        )

        setContent {
            CafecontableTheme {
                InventarioScreen(inventarioVm)
            }
        }
    }
}


//package com.ethandev.cafecontable
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import com.ethandev.cafecontable.core.AppModule
//import com.ethandev.cafecontable.ui.screen.compras.CompraCafeScreen
//import com.ethandev.cafecontable.ui.screen.compras.CompraCafeViewModel
//import com.ethandev.cafecontable.ui.theme.CafecontableTheme
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        val registrarCompraUC = AppModule.provideCompraUseCase(this)
//        val compraVm = CompraCafeViewModel(registrarCompraUC)
//
//        setContent {
//            CafecontableTheme {
//                CompraCafeScreen(compraVm)
//            }
//        }
//    }
//}