package com.ethandev.cafecontable.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ethandev.cafecontable.data.local.Converters
import com.ethandev.cafecontable.data.local.dao.*
import com.ethandev.cafecontable.data.local.entity.*

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
        AsignacionMezclaPedidoEntity::class,
        LiquidacionEntregaEntity::class,
        OperacionEntity::class,
        GastoOperacionEntity::class,
        UtilidadOperacionEntity::class,
        MezclaDespachoEntity::class
    ],
    version = 32,
    exportSchema = true
)
@TypeConverters(Converters::class)
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
    abstract fun liquidacionDao(): LiquidacionDao
    abstract fun operacionDao(): OperacionDao
    abstract fun gastoOperacionDao(): GastoOperacionDao
    abstract fun utilidadOperacionDao(): UtilidadOperacionDao
    abstract fun mezclaDespachoDao(): MezclaDespachoDao

    companion object {
        val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `mezcla_despacho` (
                    `mezclaId` TEXT NOT NULL,
                    `operacionDespachoId` TEXT NOT NULL,
                    `kgVinculados` REAL NOT NULL,
                    `fechaVinculacion` INTEGER NOT NULL,
                    PRIMARY KEY(`mezclaId`, `operacionDespachoId`)
                )""".trimIndent())
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cafe_contable_db"
                )
                    .addMigrations(MIGRATION_31_32)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}