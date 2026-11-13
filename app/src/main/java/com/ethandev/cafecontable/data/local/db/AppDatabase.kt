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
        MezclaDespachoEntity::class,
        OperacionPendienteSyncEntity::class
    ],
    version = 33,
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
    abstract fun operacionPendienteSyncDao(): OperacionPendienteSyncDao

    companion object {

        val MIGRATION_32_33 = object : Migration(32, 33) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
            CREATE TABLE IF NOT EXISTS
            `operaciones_pendientes_sync` (
                `id` TEXT NOT NULL,
                `empresaId` TEXT NOT NULL,
                `usuarioId` TEXT NOT NULL,
                `tipoOperacion` TEXT NOT NULL,
                `referenciaLocal` TEXT NOT NULL,
                `payloadJson` TEXT NOT NULL,
                `estado` TEXT NOT NULL,
                `fechaCreacion` INTEGER NOT NULL,
                `intentos` INTEGER NOT NULL,
                `ultimoError` TEXT,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

                db.execSQL("""
            CREATE INDEX IF NOT EXISTS
            `index_operaciones_pendientes_sync_empresaId_estado`
            ON `operaciones_pendientes_sync`
            (`empresaId`, `estado`)
        """.trimIndent())
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
                    .addMigrations(MIGRATION_32_33)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}