//package com.ethandev.cafecontable.core
//
//class AppModule {
//}

package com.ethandev.cafecontable.core

import android.content.Context
import androidx.room.Room
import com.ethandev.cafecontable.data.local.db.AppDatabase
import com.ethandev.cafecontable.data.repository.ProductoRepositoryImpl
import com.ethandev.cafecontable.domain.usecase.CrearProductoUseCase
import com.ethandev.cafecontable.domain.usecase.ListarProductosUseCase
import com.ethandev.cafecontable.domain.repository.ProductoRepository
import com.ethandev.cafecontable.data.repository.CompraRepositoryImpl
import com.ethandev.cafecontable.data.repository.CuentaPorCobrarRepositoryImpl
import com.ethandev.cafecontable.data.repository.CuentaPorPagarRepositoryImpl
import com.ethandev.cafecontable.data.repository.HistorialCompraRepositoryImpl
import com.ethandev.cafecontable.data.repository.HistorialVentaRepositoryImpl
import com.ethandev.cafecontable.data.repository.InventarioRepositoryImpl
import com.ethandev.cafecontable.data.repository.MezclaRepositoryImpl
import com.ethandev.cafecontable.data.repository.PreparacionEntregaRepositoryImpl
import com.ethandev.cafecontable.data.repository.PrestamoRepositoryImpl
import com.ethandev.cafecontable.data.repository.VentaPedidoRepositoryImpl
import com.ethandev.cafecontable.data.repository.VentaRepositoryImpl
import com.ethandev.cafecontable.domain.repository.CompraRepository
import com.ethandev.cafecontable.domain.repository.HistorialCompraRepository
import com.ethandev.cafecontable.domain.repository.InventarioRepository
import com.ethandev.cafecontable.domain.repository.MezclaRepository
import com.ethandev.cafecontable.domain.repository.PreparacionEntregaRepository
import com.ethandev.cafecontable.domain.repository.VentaPedidoRepository
import com.ethandev.cafecontable.domain.repository.VentaRepository
import com.ethandev.cafecontable.domain.usecase.ActualizarCompraUseCase
import com.ethandev.cafecontable.domain.usecase.ActualizarEstadoMezclaUseCase
import com.ethandev.cafecontable.domain.usecase.ActualizarMovimientoKardexUseCase
import com.ethandev.cafecontable.domain.usecase.AnularCompraUseCase
import com.ethandev.cafecontable.domain.usecase.AsignarMezclaAPedidoUseCase
import com.ethandev.cafecontable.domain.usecase.BuscarPrestamosUseCase
import com.ethandev.cafecontable.domain.usecase.FinalizarVentaPedidoUseCase
import com.ethandev.cafecontable.domain.usecase.ListarAbonosCuentaPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarAbonosCuentasPorCobrarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarAbonosPrestamoUseCase
import com.ethandev.cafecontable.domain.usecase.ListarCuentasPorCobrarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarCuentasPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.ListarPrestamosUseCase
import com.ethandev.cafecontable.domain.usecase.ListarVentasPedidoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaAnalizadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaEntregadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarMezclaPendienteEntregaUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarVentaPedidoComoAnalizadoUseCase
import com.ethandev.cafecontable.domain.usecase.MarcarVentaPedidoComoEntregadoUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerCompraPorIdUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerExistenciaUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialComprasUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialMezclasUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialVentasUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerMovimientoKardexPorCompraIdUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoCuentaPorCobrarUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoCuentaPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoPrestamoUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarCompraCafeUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarEntradaInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarMezclaUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarPreparacionEntregaUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarPrestamoUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarVentaCafeUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarVentaPedidoUseCase
import com.ethandev.cafecontable.ui.screen.asignacionmezcla.AsignacionMezclaPedidoViewModel
import com.ethandev.cafecontable.ui.screen.historialcompras.HistorialComprasViewModel
import com.ethandev.cafecontable.ui.screen.inventario.InventarioViewModel
import com.ethandev.cafecontable.ui.screen.mezcla.MezclasViewModel
import com.ethandev.cafecontable.ui.screen.preparacionentrega.PreparacionEntregaViewModel
import com.ethandev.cafecontable.ui.screen.ventaspedido.VentasPedidoViewModel


object AppModule {

    private var db: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return db ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "cafecontable.db"
        ).fallbackToDestructiveMigration()
        .build()
        .also { db = it }
    }

    fun provideProductoRepository(context: Context): ProductoRepository {
        val database = provideDatabase(context)
        return ProductoRepositoryImpl(database.productoDao())
    }

    fun provideProductoUseCases(context: Context): Pair<CrearProductoUseCase, ListarProductosUseCase> {
        val repo = provideProductoRepository(context)
        return CrearProductoUseCase(repo) to ListarProductosUseCase(repo)
    }

    fun provideCompraRepository(context: android.content.Context): CompraRepository {
        val database = provideDatabase(context)
        return CompraRepositoryImpl(database)
    }

    fun provideCompraUseCase(context: android.content.Context): RegistrarCompraCafeUseCase {
        val repo = provideCompraRepository(context)
        return RegistrarCompraCafeUseCase(repo)
    }

    fun provideInventarioRepository(context: Context): InventarioRepository {
        val database = provideDatabase(context)
        return InventarioRepositoryImpl(database.inventarioDao())
    }

    fun provideInventarioUseCases(context: Context): Triple<
            RegistrarEntradaInventarioUseCase,
            ObtenerExistenciaUseCase,
            ListarInventarioUseCase
            > {
        val repo = provideInventarioRepository(context)
        return Triple(
            RegistrarEntradaInventarioUseCase(repo),
            ObtenerExistenciaUseCase(repo),
            ListarInventarioUseCase(repo)
        )
    }

    fun provideVentaRepository(context: Context): VentaRepository {
        val database = provideDatabase(context)
        return VentaRepositoryImpl(database)
    }

    fun provideVentaUseCase(context: Context): RegistrarVentaCafeUseCase {
        val repo = provideVentaRepository(context)
        return RegistrarVentaCafeUseCase(repo)
    }

    fun provideHistorialCompraUseCase(context: Context): ObtenerHistorialComprasUseCase {
        val repo = HistorialCompraRepositoryImpl(provideDatabase(context).compraDao(),
            provideDatabase(context).inventarioDao())
        return ObtenerHistorialComprasUseCase(repo)
    }

    fun provideHistorialVentaUseCase(context: Context): ObtenerHistorialVentasUseCase {
        val repo = HistorialVentaRepositoryImpl(provideDatabase(context).ventaDao())
        return ObtenerHistorialVentasUseCase(repo)
    }

    fun provideCuentaPorCobrarUseCase(context: Context): ListarCuentasPorCobrarUseCase {
        val repo = CuentaPorCobrarRepositoryImpl(provideDatabase(context).cuentaPorCobrarDao())
        return ListarCuentasPorCobrarUseCase(repo)
    }

    fun provideRegistrarAbonoCuentaPorCobrarUseCase(context: Context): RegistrarAbonoCuentaPorCobrarUseCase {
        val repo = CuentaPorCobrarRepositoryImpl(provideDatabase(context).cuentaPorCobrarDao())
        return RegistrarAbonoCuentaPorCobrarUseCase(repo)
    }

    fun provideListarAbonosCuentasPorCobrarUseCase(context: Context): ListarAbonosCuentasPorCobrarUseCase {
        val repo = CuentaPorCobrarRepositoryImpl(provideDatabase(context).cuentaPorCobrarDao())
        return ListarAbonosCuentasPorCobrarUseCase(repo)
    }

    fun provideCuentaPorPagarUseCase(context: Context): ListarCuentasPorPagarUseCase {
        val repo = CuentaPorPagarRepositoryImpl(provideDatabase(context).cuentaPorPagarDao())
        return ListarCuentasPorPagarUseCase(repo)
    }

    fun provideRegistrarAbonoCuentaPorPagarUseCase(context: Context): RegistrarAbonoCuentaPorPagarUseCase {
        val repo = CuentaPorPagarRepositoryImpl(provideDatabase(context).cuentaPorPagarDao())
        return RegistrarAbonoCuentaPorPagarUseCase(repo)
    }

    fun provideListarAbonosCuentaPorPagarUseCase(context: Context): ListarAbonosCuentaPorPagarUseCase {
        val repo = CuentaPorPagarRepositoryImpl(provideDatabase(context).cuentaPorPagarDao())
        return ListarAbonosCuentaPorPagarUseCase(repo)
    }

    fun provideRegistrarPrestamoUseCase (context: Context): RegistrarPrestamoUseCase {
        val repo = PrestamoRepositoryImpl(
            provideDatabase(context).prestamoDao(),
            provideDatabase(context).abonoPrestamoDao()
        )
        return RegistrarPrestamoUseCase(repo)
    }

    fun provideListarPrestamosUseCase  (context: Context): ListarPrestamosUseCase {
        val repo = PrestamoRepositoryImpl(
            provideDatabase(context).prestamoDao(),
            provideDatabase(context).abonoPrestamoDao()
        )
        return ListarPrestamosUseCase(repo)
    }

    fun provideBuscarPrestamosUseCase  (context: Context): BuscarPrestamosUseCase {
        val repo = PrestamoRepositoryImpl(
            provideDatabase(context).prestamoDao(),
            provideDatabase(context).abonoPrestamoDao()
        )
        return BuscarPrestamosUseCase(repo)
    }

    fun provideRegistrarAbonoPrestamoUseCase  (context: Context): RegistrarAbonoPrestamoUseCase {
        val repo = PrestamoRepositoryImpl(
            provideDatabase(context).prestamoDao(),
            provideDatabase(context).abonoPrestamoDao()
        )
        return RegistrarAbonoPrestamoUseCase(repo)
    }

    fun provideListarAbonoPrestamoUseCase  (context: Context): ListarAbonosPrestamoUseCase {
        val repo = PrestamoRepositoryImpl(
            provideDatabase(context).prestamoDao(),
            provideDatabase(context).abonoPrestamoDao()
        )
        return ListarAbonosPrestamoUseCase(repo)
    }


//    fun provideHistorialCompraRepository(context: Context): HistorialCompraRepository {
//        val dao = provideDatabase(context).compraDao()
//        return HistorialCompraRepositoryImpl(dao)
//    }

    fun provideObtenerHistorialComprasUseCase(context: Context): ObtenerHistorialComprasUseCase {
        val repo = provideHistorialCompraRepository(context)
        return ObtenerHistorialComprasUseCase(repo)
    }

    fun provideAnularCompraUseCase(context: Context): AnularCompraUseCase {
        val repo = provideHistorialCompraRepository(context)
        return AnularCompraUseCase(repo)
    }

    fun provideObtenerCompraPorIdUseCase(context: Context): ObtenerCompraPorIdUseCase {
        val repo = provideHistorialCompraRepository(context)
        return ObtenerCompraPorIdUseCase(repo)
    }

    fun provideActualizarCompraUseCase(context: Context): ActualizarCompraUseCase {
        val repo = provideHistorialCompraRepository(context)
        return ActualizarCompraUseCase(repo)
    }

    fun provideHistorialComprasViewModel(context: Context): HistorialComprasViewModel {
        return HistorialComprasViewModel(
            obtenerHistorialComprasUseCase = provideObtenerHistorialComprasUseCase(context),
            anularCompraUseCase = provideAnularCompraUseCase(context),
            obtenerCompraPorIdUseCase = provideObtenerCompraPorIdUseCase(context),
            actualizarCompraUseCase = provideActualizarCompraUseCase(context),
            obtenerMovimientoKardexPorCompraIdUseCase = provideObtenerMovimientoKardexPorCompraIdUseCase(context),
            actualizarMovimientoKardexUseCase = provideActualizarMovimientoKardexUseCase(context)

        )
    }

    fun provideHistorialCompraRepository(context: Context): HistorialCompraRepository {
        val db = provideDatabase(context)
        return HistorialCompraRepositoryImpl(
            compraDao = db.compraDao(),
            inventarioDao = db.inventarioDao()
        )
    }

    fun provideObtenerMovimientoKardexPorCompraIdUseCase(context: Context): ObtenerMovimientoKardexPorCompraIdUseCase {
        val repo = provideHistorialCompraRepository(context)
        return ObtenerMovimientoKardexPorCompraIdUseCase(repo)
    }

    fun provideActualizarMovimientoKardexUseCase(context: Context): ActualizarMovimientoKardexUseCase {
        val repo = provideHistorialCompraRepository(context)
        return ActualizarMovimientoKardexUseCase(repo)
    }


    fun provideObtenerInventarioUseCase(context: Context): ObtenerInventarioUseCase {
        return ObtenerInventarioUseCase(
            repository = provideInventarioRepository(context)
        )
    }

    fun provideObtenerExistenciaUseCase(context: Context): ObtenerExistenciaUseCase {
        val repository = provideInventarioRepository(context)
        return ObtenerExistenciaUseCase(repository)
    }

    fun provideInventarioViewModel(context: Context): InventarioViewModel {
        return InventarioViewModel(
            obtenerInventarioUseCase = provideObtenerInventarioUseCase(context)
        )
    }

//    fun provideVentaPedidoRepository(context: Context): VentaPedidoRepository {
//        return VentaPedidoRepositoryImpl(
//            db = provideDatabase(context)
//        )
//    }
//
//    fun provideRegistrarVentaPedidoUseCase(context: Context): RegistrarVentaPedidoUseCase {
//        return RegistrarVentaPedidoUseCase(
//            repository = provideVentaPedidoRepository(context)
//        )
//    }
//
//    fun provideListarVentasPedidoUseCase(context: Context): ListarVentasPedidoUseCase {
//        return ListarVentasPedidoUseCase(
//            repository = provideVentaPedidoRepository(context)
//        )
//    }
//
//    fun provideVentasPedidoViewModel(context: Context): VentasPedidoViewModel {
//        return VentasPedidoViewModel(
//            registrarVentaPedidoUseCase = provideRegistrarVentaPedidoUseCase(context),
//            listarVentasPedidoUseCase = provideListarVentasPedidoUseCase(context)
//        )
//    }
//
//    fun providePreparacionEntregaRepository(context: Context): PreparacionEntregaRepository {
//        return PreparacionEntregaRepositoryImpl(
//            db = provideDatabase(context)
//        )
//    }
//
//    fun provideRegistrarPreparacionEntregaUseCase(context: Context): RegistrarPreparacionEntregaUseCase {
//        val db = provideDatabase(context)
//
//        return RegistrarPreparacionEntregaUseCase(
//            inventarioDao = db.inventarioDao(),
//            preparacionEntregaDao = db.preparacionEntregaDao(),
//            ventaPedidoDao = db.ventaPedidoDao()
//        )
//    }
//
//
//    fun providePreparacionEntregaViewModel(context: Context): PreparacionEntregaViewModel {
//        return PreparacionEntregaViewModel(
//            registrarPreparacionEntregaUseCase = provideRegistrarPreparacionEntregaUseCase(context)
//        )
//    }
//
////
////    fun provideVentaPedidoRepository(context: Context): VentaPedidoRepositoryImpl {
////        return VentaPedidoRepositoryImpl(
////            db = provideDatabase(context)
////        )
////    }
//
//    fun provideRegistrarMezclaUseCase(context: Context): RegistrarMezclaUseCase {
//        val db = provideDatabase(context)
//        return RegistrarMezclaUseCase(
//            mezclaDao = db.mezclaDao(),
//            inventarioDao = db.inventarioDao()
//        )
//    }
//
//    fun provideAsignarMezclaAPedidoUseCase(context: Context): AsignarMezclaAPedidoUseCase {
//        val db = provideDatabase(context)
//        return AsignarMezclaAPedidoUseCase(
//            ventaPedidoDao = db.ventaPedidoDao(),
//            mezclaDao = db.mezclaDao(),
//            asignacionDao = db.asignacionMezclaPedidoDao()
//        )
//    }
//
//    fun provideMezclasViewModel(context: Context): MezclasViewModel {
//        val db = provideDatabase(context)
//        return MezclasViewModel(
//            registrarMezclaUseCase = provideRegistrarMezclaUseCase(context),
//            productoDao = db.productoDao(),
//            compraCafeDao = db.compraDao()
//        )
//    }
//
//    fun provideMezclaRepository(context: Context): MezclaRepository {
//        return MezclaRepositoryImpl(
//            db = provideDatabase(context)
//        )
//    }
//
//    fun provideRegistrarMezclaUseCase(context: Context): RegistrarMezclaUseCase {
//        return RegistrarMezclaUseCase(
//            repository = provideMezclaRepository(context)
//        )
//    }
//
//    fun provideAsignacionMezclaPedidoViewModel(context: Context): AsignacionMezclaPedidoViewModel {
//        val db = provideDatabase(context)
//        return AsignacionMezclaPedidoViewModel(
//            asignarMezclaAPedidoUseCase = provideAsignarMezclaAPedidoUseCase(context),
//            ventaPedidoDao = db.ventaPedidoDao(),
//            mezclaDao = db.mezclaDao()
//        )
//    }



    //-------------------------------------------------------------------------------------

//fun provideVentaPedidoRepository(context: Context): VentaPedidoRepository {
//    return VentaPedidoRepositoryImpl(
//        db = provideDatabase(context)
//    )
//}
//
//    fun provideRegistrarVentaPedidoUseCase(context: Context): RegistrarVentaPedidoUseCase {
//        return RegistrarVentaPedidoUseCase(
//            repository = provideVentaPedidoRepository(context)
//        )
//    }
//
//    fun provideListarVentasPedidoUseCase(context: Context): ListarVentasPedidoUseCase {
//        return ListarVentasPedidoUseCase(
//            repository = provideVentaPedidoRepository(context)
//        )
//    }
//
//    fun provideVentasPedidoViewModel(context: Context): VentasPedidoViewModel {
//        return VentasPedidoViewModel(
//            registrarVentaPedidoUseCase = provideRegistrarVentaPedidoUseCase(context),
//            listarVentasPedidoUseCase = provideListarVentasPedidoUseCase(context)
//        )
//    }
//
//    fun provideMezclaRepository(context: Context): MezclaRepository {
//        val db = provideDatabase(context)
//        return MezclaRepositoryImpl(
//            db = db,
//            mezclaDao = db.mezclaDao(),
//            inventarioDao = db.inventarioDao()
//        )
//    }
//
//    fun provideRegistrarMezclaUseCase(context: Context): RegistrarMezclaUseCase {
//        return RegistrarMezclaUseCase(
//            repository = provideMezclaRepository(context)
//        )
//    }
//
//    fun provideAsignarMezclaAPedidoUseCase(context: Context): AsignarMezclaAPedidoUseCase {
//        val db = provideDatabase(context)
//        return AsignarMezclaAPedidoUseCase(
//            ventaPedidoDao = db.ventaPedidoDao(),
//            mezclaDao = db.mezclaDao(),
//            asignacionDao = db.asignacionMezclaPedidoDao()
//        )
//    }
//
//    fun provideMezclasViewModel(context: Context): MezclasViewModel {
//        val db = provideDatabase(context)
//        return MezclasViewModel(
//            registrarMezclaUseCase = provideRegistrarMezclaUseCase(context),
//            productoDao = db.productoDao(),
//            compraCafeDao = db.compraDao()
//        )
//    }
//
//    fun provideAsignacionMezclaPedidoViewModel(context: Context): AsignacionMezclaPedidoViewModel {
//        val db = provideDatabase(context)
//        return AsignacionMezclaPedidoViewModel(
//            asignarMezclaAPedidoUseCase = provideAsignarMezclaAPedidoUseCase(context),
//            ventaPedidoDao = db.ventaPedidoDao(),
//            mezclaDao = db.mezclaDao()
//        )
//    }

    fun provideVentaPedidoRepository(context: Context): VentaPedidoRepository {
        return VentaPedidoRepositoryImpl(
            db = provideDatabase(context)
        )
    }




    ///////////////////////-----------------------------


    fun provideRegistrarVentaPedidoUseCase(context: Context): RegistrarVentaPedidoUseCase {
        return RegistrarVentaPedidoUseCase(
            repository = provideVentaPedidoRepository(context)
        )
    }

    fun provideListarVentasPedidoUseCase(context: Context): ListarVentasPedidoUseCase {
        return ListarVentasPedidoUseCase(
            repository = provideVentaPedidoRepository(context)
        )
    }

    fun provideMarcarVentaPedidoComoEntregadoUseCase(context: Context): MarcarVentaPedidoComoEntregadoUseCase {
        return MarcarVentaPedidoComoEntregadoUseCase(
            repository = provideVentaPedidoRepository(context)
        )
    }

    fun provideMarcarVentaPedidoComoAnalizadoUseCase(context: Context): MarcarVentaPedidoComoAnalizadoUseCase {
        return MarcarVentaPedidoComoAnalizadoUseCase(
            repository = provideVentaPedidoRepository(context)
        )
    }

    fun provideFinalizarVentaPedidoUseCase(context: Context): FinalizarVentaPedidoUseCase {
        return FinalizarVentaPedidoUseCase(
            repository = provideVentaPedidoRepository(context)
        )
    }

//    fun provideVentasPedidoViewModel(context: Context): VentasPedidoViewModel {
//        return VentasPedidoViewModel(
//            registrarVentaPedidoUseCase = provideRegistrarVentaPedidoUseCase(context),
//            listarVentasPedidoUseCase = provideListarVentasPedidoUseCase(context),
//            marcarVentaPedidoComoEntregadoUseCase = provideMarcarVentaPedidoComoEntregadoUseCase
//        )
//    }

    fun provideVentasPedidoViewModel(context: Context): VentasPedidoViewModel {
        return VentasPedidoViewModel(
            registrarVentaPedidoUseCase = provideRegistrarVentaPedidoUseCase(context),
            listarVentasPedidoUseCase = provideListarVentasPedidoUseCase(context),
            marcarVentaPedidoComoEntregadoUseCase = provideMarcarVentaPedidoComoEntregadoUseCase(context),
            marcarVentaPedidoComoAnalizadoUseCase = provideMarcarVentaPedidoComoAnalizadoUseCase(context),
            finalizarVentaPedidoUseCase = provideFinalizarVentaPedidoUseCase(context)
        )
    }

    fun providePreparacionEntregaRepository(context: Context): PreparacionEntregaRepository {
        return PreparacionEntregaRepositoryImpl(
            db = provideDatabase(context)
        )
    }

    fun provideRegistrarPreparacionEntregaUseCase(context: Context): RegistrarPreparacionEntregaUseCase {
        val db = provideDatabase(context)
        return RegistrarPreparacionEntregaUseCase(
            inventarioDao = db.inventarioDao(),
            preparacionEntregaDao = db.preparacionEntregaDao(),
            ventaPedidoDao = db.ventaPedidoDao()
        )
    }

    fun providePreparacionEntregaViewModel(context: Context): PreparacionEntregaViewModel {
        return PreparacionEntregaViewModel(
            registrarPreparacionEntregaUseCase = provideRegistrarPreparacionEntregaUseCase(context)
        )
    }

    fun provideMezclaRepository(context: Context): MezclaRepository {
        val db = provideDatabase(context)
        return MezclaRepositoryImpl(
            db = db,
            mezclaDao = db.mezclaDao(),
            inventarioDao = db.inventarioDao()
        )
    }

    fun provideRegistrarMezclaUseCase(context: Context): RegistrarMezclaUseCase {
        return RegistrarMezclaUseCase(
            repository = provideMezclaRepository(context)
        )
    }

    fun provideAsignarMezclaAPedidoUseCase(context: Context): AsignarMezclaAPedidoUseCase {
        val db = provideDatabase(context)
        return AsignarMezclaAPedidoUseCase(
            ventaPedidoDao = db.ventaPedidoDao(),
            mezclaDao = db.mezclaDao(),
            asignacionDao = db.asignacionMezclaPedidoDao()
        )
    }
    fun provideObtenerHistorialMezclasUseCase(context: Context): ObtenerHistorialMezclasUseCase {
        return ObtenerHistorialMezclasUseCase(
            repository = provideMezclaRepository(context)
        )
    }

    fun provideMezclasViewModel(context: Context): MezclasViewModel {
        val db = provideDatabase(context)
        return MezclasViewModel(
            registrarMezclaUseCase = provideRegistrarMezclaUseCase(context),
            obtenerHistorialMezclasUseCase = provideObtenerHistorialMezclasUseCase(context),
          //  actualizarEstadoMezclaUseCase = provideActualizarEstadoMezclaUseCase(context),
            marcarMezclaAnalizadoUseCase = provideMarcarAnalizadoUseCase(context),
            marcarMezclaEntregadoUseCase = provideMarcarEntregadoUseCase(context),
            marcarMezclaPendienteEntregaUseCase = provideMarcarPendienteEntregaUseCase(context),
            compraCafeDao = db.compraDao()
        )
    }

    fun provideAsignacionMezclaPedidoViewModel(context: Context): AsignacionMezclaPedidoViewModel {
        val db = provideDatabase(context)
        return AsignacionMezclaPedidoViewModel(
            asignarMezclaAPedidoUseCase = provideAsignarMezclaAPedidoUseCase(context),
            ventaPedidoDao = db.ventaPedidoDao(),
            mezclaDao = db.mezclaDao()
        )
    }

    fun provideActualizarEstadoMezclaUseCase(context: Context): ActualizarEstadoMezclaUseCase {
        return ActualizarEstadoMezclaUseCase(
            repository = provideMezclaRepository(context)
        )
    }

    fun provideMarcarPendienteEntregaUseCase(context: Context) =
        MarcarMezclaPendienteEntregaUseCase(provideMezclaRepository(context))

    fun provideMarcarEntregadoUseCase(context: Context) =
        MarcarMezclaEntregadoUseCase(provideMezclaRepository(context))

    fun provideMarcarAnalizadoUseCase(context: Context) =
        MarcarMezclaAnalizadoUseCase(provideMezclaRepository(context))
}