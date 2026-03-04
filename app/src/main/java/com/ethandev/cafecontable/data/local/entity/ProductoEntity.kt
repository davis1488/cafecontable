//package com.ethandev.cafecontable.data.local.entity
//
//class ProductoEntity {
//
//}

package com.ethandev.cafecontable.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "producto")
data class ProductoEntity(


//    val id: String,
//
//    val nombre: String,
//
//    val unidad: String,
//
//    val precioVenta: Long

    @PrimaryKey val id: String,
    val nombre: String,
    val unidad: String,
    val precioVenta: Long,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()

)