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
import com.ethandev.cafecontable.data.repository.InventarioRepositoryImpl
import com.ethandev.cafecontable.domain.repository.CompraRepository
import com.ethandev.cafecontable.domain.repository.InventarioRepository
import com.ethandev.cafecontable.domain.usecase.ListarInventarioUseCase
import com.ethandev.cafecontable.domain.usecase.ObtenerExistenciaUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarCompraCafeUseCase
import com.ethandev.cafecontable.domain.usecase.RegistrarEntradaInventarioUseCase


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
    }}