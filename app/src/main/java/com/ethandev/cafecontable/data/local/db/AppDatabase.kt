package com.ethandev.cafecontable.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ethandev.cafecontable.data.dao.AbonoPrestamoDao
import com.ethandev.cafecontable.data.dao.PrestamoDao
import com.ethandev.cafecontable.data.entity.AbonoPrestamoEntity
import com.ethandev.cafecontable.data.entity.PrestamoEntity
import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.dao.CuentaPorCobrarDao
import com.ethandev.cafecontable.data.local.dao.CuentaPorPagarDao
import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.dao.ProductoDao
import com.ethandev.cafecontable.data.local.dao.VentaDao
import com.ethandev.cafecontable.data.local.entity.AbonoCuentaPorCobrarEntity
import com.ethandev.cafecontable.data.local.entity.AbonoCuentaPorPagarEntity
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.CuentaPorCobrarEntity
import com.ethandev.cafecontable.data.local.entity.CuentaPorPagarEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.ProductoEntity
import com.ethandev.cafecontable.data.local.entity.VentaCafeEntity

@Database(
    entities = [
        ProductoEntity::class,
        KardexMovimientoEntity::class,
        CompraCafeEntity::class,
        VentaCafeEntity::class,
        CuentaPorCobrarEntity::class,
        AbonoCuentaPorCobrarEntity::class,
        CuentaPorPagarEntity::class,
        AbonoCuentaPorPagarEntity::class,
        PrestamoEntity::class,
        AbonoPrestamoEntity::class
               ],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun compraDao(): CompraDao
    abstract fun ventaDao(): VentaDao
    abstract fun cuentaPorCobrarDao(): CuentaPorCobrarDao
    abstract fun cuentaPorPagarDao(): CuentaPorPagarDao
    abstract fun prestamoDao(): PrestamoDao
    abstract fun abonoPrestamoDao(): AbonoPrestamoDao
}