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
object AppModule {

    private var db: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return db ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "cafecontable.db"
        ).build().also { db = it }
    }

    fun provideProductoRepository(context: Context): ProductoRepository {
        val database = provideDatabase(context)
        return ProductoRepositoryImpl(database.productoDao())
    }

    fun provideProductoUseCases(context: Context): Pair<CrearProductoUseCase, ListarProductosUseCase> {
        val repo = provideProductoRepository(context)
        return CrearProductoUseCase(repo) to ListarProductosUseCase(repo)
    }
}