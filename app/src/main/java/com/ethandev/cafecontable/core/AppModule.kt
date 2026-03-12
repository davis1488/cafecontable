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
import com.ethandev.cafecontable.data.repository.VentaRepositoryImpl
import com.ethandev.cafecontable.domain.repository.CompraRepository
import com.ethandev.cafecontable.domain.repository.InventarioRepository
import com.ethandev.cafecontable.domain.repository.VentaRepository
import com.ethandev.cafecontable.domain.usecase.ListarAbonosCuentaPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarAbonosCuentasPorCobrarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarCuentasPorCobrarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarCuentasPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.ListarInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerExistenciaUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialComprasUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerHistorialVentasUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoCuentaPorCobrarUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarAbonoCuentaPorPagarUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarCompraCafeUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarEntradaInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarVentaCafeUseCase


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
        val repo = HistorialCompraRepositoryImpl(provideDatabase(context).compraDao())
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
}