package com.ethandev.cafecontable.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ethandev.cafecontable.data.dao.AbonoPrestamoDao
import com.ethandev.cafecontable.data.dao.PrestamoDao
import com.ethandev.cafecontable.data.entity.AbonoPrestamoEntity
import com.ethandev.cafecontable.data.entity.PrestamoEntity
import com.ethandev.cafecontable.data.local.dao.AsignacionMezclaPedidoDao
import com.ethandev.cafecontable.data.local.dao.CompraDao
import com.ethandev.cafecontable.data.local.dao.CuentaPorCobrarDao
import com.ethandev.cafecontable.data.local.dao.CuentaPorPagarDao
import com.ethandev.cafecontable.data.local.dao.InventarioDao
import com.ethandev.cafecontable.data.local.dao.MezclaDao
import com.ethandev.cafecontable.data.local.dao.PreparacionEntregaDao
import com.ethandev.cafecontable.data.local.dao.ProductoDao
import com.ethandev.cafecontable.data.local.dao.VentaDao
import com.ethandev.cafecontable.data.local.dao.VentaPedidoDao
import com.ethandev.cafecontable.data.local.entity.AbonoCuentaPorCobrarEntity
import com.ethandev.cafecontable.data.local.entity.AbonoCuentaPorPagarEntity
import com.ethandev.cafecontable.data.local.entity.AsignacionMezclaPedidoEntity
import com.ethandev.cafecontable.data.local.entity.CompraCafeEntity
import com.ethandev.cafecontable.data.local.entity.CuentaPorCobrarEntity
import com.ethandev.cafecontable.data.local.entity.CuentaPorPagarEntity
import com.ethandev.cafecontable.data.local.entity.KardexMovimientoEntity
import com.ethandev.cafecontable.data.local.entity.MezclaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.MezclaEntity
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaDetalleEntity
import com.ethandev.cafecontable.data.local.entity.PreparacionEntregaEntity
import com.ethandev.cafecontable.data.local.entity.ProductoEntity
import com.ethandev.cafecontable.data.local.entity.VentaCafeEntity
import com.ethandev.cafecontable.data.local.entity.VentaPedidoEntity

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
        AbonoPrestamoEntity::class,
        PreparacionEntregaEntity::class,
        PreparacionEntregaDetalleEntity::class,
        VentaPedidoEntity::class,
        MezclaEntity::class,
        MezclaDetalleEntity::class,
        AsignacionMezclaPedidoEntity::class

               ],
    version = 17,
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
    abstract fun ventaPedidoDao(): VentaPedidoDao
    abstract fun preparacionEntregaDao(): PreparacionEntregaDao

    abstract fun mezclaDao(): MezclaDao
    abstract fun asignacionMezclaPedidoDao(): AsignacionMezclaPedidoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cafe_contable_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

}