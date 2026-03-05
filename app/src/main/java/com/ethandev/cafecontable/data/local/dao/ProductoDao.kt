//package com.ethandev.cafecontable.data.local.dao
//
//class ProductoDao {
//}
//

package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ethandev.cafecontable.data.local.entity.ProductoEntity

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(producto: ProductoEntity)

    @Query("SELECT * FROM producto WHERE activo = 1 ORDER BY nombre")
    suspend fun listar(): List<ProductoEntity>

    @Query("SELECT * FROM producto WHERE nombre = :nombre LIMIT 1")
    suspend fun getByNombre(nombre: String) :ProductoEntity?

    @Insert (onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(producto: ProductoEntity)
}