
package com.ethandev.cafecontable.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.data.remote.AuthTokenManager
import kotlinx.coroutines.CancellationException

class SincronizarComprasWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // El proceso puede haber sido cerrado por Android.
            AuthTokenManager.inicializar(applicationContext)

            val token = AuthTokenManager.obtenerToken()
                ?: AuthTokenManager.restaurarToken()

            if (token.isNullOrBlank() ||
                !AuthTokenManager.tokenVigente(token)
            ) {
                // La sesión offline sigue disponible.
                // Sin JWT válido no intentamos sincronizar.
                return Result.success()
            }

            val sincronizador =
                AppModule.provideSincronizarComprasPendientes(
                    applicationContext
                )

            sincronizador()

            // Los errores individuales quedan registrados en Room.
            // El siguiente ciclo puede reintentarlos.
            Result.success()

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            android.util.Log.e(
                "SYNC_COMPRAS",
                "Error en sincronización automática",
                e
            )

            // Conservamos los pendientes para el siguiente ciclo.
            Result.success()
        }
    }
}
