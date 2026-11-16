
package com.ethandev.cafecontable.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ProgramadorSyncCompras {

    private const val NOMBRE_TRABAJO =
        "cafecontable_sync_compras"

    fun iniciar(context: Context) {

        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(
                NetworkType.CONNECTED
            )
            .build()

        val solicitud =
            PeriodicWorkRequestBuilder<
                    SincronizarComprasWorker
                    >(15, TimeUnit.MINUTES)
                .setConstraints(restricciones)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                NOMBRE_TRABAJO,
                ExistingPeriodicWorkPolicy.KEEP,
                solicitud
            )
    }

    fun detener(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(NOMBRE_TRABAJO)
    }
}
