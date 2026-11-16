
package com.ethandev.cafecontable.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ProgramadorSyncCompras {

    private const val TRABAJO_PERIODICO =
        "cafecontable_sync_compras"

    private const val TRABAJO_INMEDIATO =
        "cafecontable_sync_compras_inmediato"

    private fun restricciones() =
        Constraints.Builder()
            .setRequiredNetworkType(
                NetworkType.CONNECTED
            )
            .build()

    // Respaldo periódico cada 15 minutos
    fun iniciar(context: Context) {

        val solicitud =
            PeriodicWorkRequestBuilder<
                    SincronizarComprasWorker
                    >(15, TimeUnit.MINUTES)
                .setConstraints(restricciones())
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                TRABAJO_PERIODICO,
                ExistingPeriodicWorkPolicy.KEEP,
                solicitud
            )
    }

    // Solicitar sincronización al guardar una compra
    fun sincronizarAhora(context: Context) {

        val solicitud =
            OneTimeWorkRequestBuilder<
                    SincronizarComprasWorker
                    >()
                .setConstraints(restricciones())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                TRABAJO_INMEDIATO,
                ExistingWorkPolicy.KEEP,
                solicitud
            )
    }

    fun detener(context: Context) {
        val workManager =
            WorkManager.getInstance(context)

        workManager.cancelUniqueWork(TRABAJO_PERIODICO)
        workManager.cancelUniqueWork(TRABAJO_INMEDIATO)
    }
}
