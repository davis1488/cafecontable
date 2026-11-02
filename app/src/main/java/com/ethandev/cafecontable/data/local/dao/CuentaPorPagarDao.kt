package com.ethandev.cafecontable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ethandev.cafecontable.data.local.entity.AbonoCuentaPorPagarEntity
import com.ethandev.cafecontable.data.local.entity.CuentaPorPagarEntity

@Dao
interface CuentaPorPagarDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(cuenta: CuentaPorPagarEntity)

    @Query("""
        SELECT * FROM cuenta_por_pagar
        ORDER BY fecha DESC
    """)
    suspend fun  listarPendientes(): List<CuentaPorPagarEntity>

    @Query("""
        SELECT * FROM cuenta_por_pagar
        WHERE estado = 'PENDIENTE'
        ORDER BY fecha DESC
    """)
    suspend fun listarTodas(): List<CuentaPorPagarEntity>

    @Query("""
        SELECT * FROM cuenta_por_pagar
        WHERE id = :cuentaId
        LIMIT 1
    """)
    suspend fun getById(cuentaId: String): CuentaPorPagarEntity?

    @Update
    suspend fun update(cuenta: CuentaPorPagarEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAbono(abono: AbonoCuentaPorPagarEntity)

    @Query("""
        SELECT * FROM abono_cuenta_por_pagar
        WHERE cuentaId = :cuentaId
        ORDER BY fecha DESC
    """)
    suspend fun listarAbonos(cuentaId: String): List<AbonoCuentaPorPagarEntity>


    @Query("""
    SELECT *
    FROM cuenta_por_pagar
    WHERE compraId = :compraId
    LIMIT 1
""")
    suspend fun obtenerPorCompraId(
        compraId: String
    ): CuentaPorPagarEntity?

    @Query("""
    SELECT COUNT(*)
    FROM abono_cuenta_por_pagar
    WHERE cuentaId = :cuentaId
""")
    suspend fun contarAbonos(
        cuentaId: String
    ): Int

    @Query("""
    UPDATE cuenta_por_pagar
    SET estado = 'ANULADA',
        saldoPendiente = 0
    WHERE id = :cuentaId
      AND estado = 'PENDIENTE'
""")
    suspend fun anularCuentaPorPagar(
        cuentaId: String
    ): Int


}