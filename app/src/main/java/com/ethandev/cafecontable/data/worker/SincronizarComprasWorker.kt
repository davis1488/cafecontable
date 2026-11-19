package com.ethandev.cafecontable.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.data.remote.AuthTokenManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SincronizarComprasWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SYNC_COMPRAS"
        private const val MAX_REINTENTOS = 5
        // Exclusión mutua entre los dos tipos de Worker en este proceso.
        private val mutex = Mutex()
    }

    override suspend fun doWork(): Result {
        return try {
            mutex.withLock {
                AuthTokenManager.inicializar(applicationContext)
                val token = AuthTokenManager.obtenerToken()
                    ?: AuthTokenManager.restaurarToken()

                if (token.isNullOrBlank() || !AuthTokenManager.tokenVigente(token)) {
                    Log.d(TAG, "JWT no vigente: pendientes conservados")
                    return@withLock Result.success()
                }

                val sincronizador = AppModule.provideSincronizarComprasPendientes(
                    applicationContext
                )
                val resultado = sincronizador()
                Log.d(TAG, "Sincronizadas=${resultado.sincronizadas}, " +
                        "conflictos=${resultado.conflictos}, pendientes=${resultado.pendientes}, " +
                        "reintentar=${resultado.requiereReintento}")

                if (resultado.requiereReintento && runAttemptCount < MAX_REINTENTOS) {
                    Result.retry()
                } else {
                    // La ejecución termina; los registros pendientes siguen en Room.
                    // Una nueva ejecución periódica podrá procesarlos.
                    Result.success()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Fallo inesperado de sincronización", e)
            if (runAttemptCount < MAX_REINTENTOS) Result.retry()
            else Result.success()
        }
    }
}
