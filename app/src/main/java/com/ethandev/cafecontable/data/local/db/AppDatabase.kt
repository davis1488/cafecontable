//package com.ethandev.cafecontable.data.local.db
//
//class AppDatabase {
//}

package com.ethandev.cafecontable.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ethandev.cafecontable.data.local.dao.ProductoDao
import com.ethandev.cafecontable.data.local.entity.ProductoEntity

@Database(
    entities = [ProductoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
}