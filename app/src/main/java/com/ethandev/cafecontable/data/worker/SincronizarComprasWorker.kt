package com.ethandev.cafecontable.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.data.remote.auth.SesionOnlineManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import com.ethandev.cafecontable.data.remote.auth.CoordinadorSesion

class SincronizarComprasWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SYNC_COMPRAS"
        private const val MAX_REINTENTOS = 5

        // Evita ejecutar simultáneamente este Worker en el mismo proceso.
    }

    override suspend fun doWork(): Result = try {
        CoordinadorSesion.mutex.withLock {
            // Primero se obtiene o renueva el JWT. Este método también intenta
            // recuperar una renovación pendiente, si existe.
            val token = SesionOnlineManager.obtenerJwtVigente(
                applicationContext
            )

            if (token.isNullOrBlank()) {
                // No hay autorización online: no tocar las compras pendientes.
                // No reintentar sin límite mientras el usuario no inicie sesión.
                Log.w(TAG, "Sin JWT disponible; compras pendientes conservadas")
                return@withLock Result.success()
            }

            // El repositorio de sincronización utiliza RetrofitClient y su
            // interceptor de Authorization; AuthTokenManager debe contener
            // el mismo JWT vigente devuelto por SesionOnlineManager.
            val sincronizador = AppModule.provideSincronizarComprasPendientes(
                applicationContext
            )
            val resultado = sincronizador()

            Log.d(
                TAG,
                "Sincronizadas=${resultado.sincronizadas}, " +
                        "conflictos=${resultado.conflictos}, " +
                        "pendientes=${resultado.pendientes}, " +
                        "reintentar=${resultado.requiereReintento}"
            )

            if (resultado.requiereReintento && runAttemptCount < MAX_REINTENTOS) {
                Result.retry()
            } else {
                // Los registros no sincronizados permanecen en Room.
                Result.success()
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Log.w(TAG, "Error de red; compras pendientes conservadas", e)
        if (runAttemptCount < MAX_REINTENTOS) Result.retry()
        else Result.success()
    } catch (e: Exception) {
        Log.e(TAG, "No se pudo completar la sincronización", e)
        if (runAttemptCount < MAX_REINTENTOS) Result.retry()
        else Result.success()
    }
}
