//package com.ethandev.cafecontable.data.local.db
//
//class AppDatabase {
//}

package com.ethandev.cafecontable.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.dao.ProductoDao
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.ProductoEntity

@Database(
    entities = [
        ProductoEntity::class,
        KardexMovimientoEntity::class,
        CompraCafeEntity::class
               ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun compraDao(): CompraDao
}