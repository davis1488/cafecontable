
package com.ethandev.cafecontable.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ProgramadorLogoutPendiente {

    private const val NOMBRE_TRABAJO =
        "cafecontable_logout_pendiente"

    fun programar(context: Context) {

        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(
                NetworkType.CONNECTED
            )
            .build()

        val solicitud =
            OneTimeWorkRequestBuilder<LogoutPendienteWorker>()
                .setConstraints(restricciones)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

        WorkManager.getInstance(
            context.applicationContext
        ).enqueueUniqueWork(
            NOMBRE_TRABAJO,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            solicitud
        )
    }
}
