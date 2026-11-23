
package com.ethandev.cafecontable.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ethandev.cafecontable.core.AppModule
import com.ethandev.cafecontable.data.remote.auth.AuthTokenManager
import com.ethandev.cafecontable.data.remote.auth.CoordinadorSesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.withLock

class LogoutPendienteWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "LOGOUT_PENDIENTE"
    }

    override suspend fun doWork(): Result {
        return try {
            CoordinadorSesion.mutex.withLock {

                // El Worker debe funcionar incluso en Login.
                // No requiere JWT ni SessionManager.
                AuthTokenManager.inicializar(
                    applicationContext
                )

                val pendientes =
                    AuthTokenManager.obtenerLogoutsPendientes()

                if (pendientes.isEmpty()) {
                    Log.d(TAG, "No existen revocaciones pendientes")
                    return@withLock Result.success()
                }

                val repository =
                    AppModule.provideDispositivoRepository()

                for (pendiente in pendientes) {

                    // procesarLogoutPendiente utiliza ECDSA
                    // y no modifica la sesión activa.
                    val confirmado =
                        repository.procesarLogoutPendiente(
                            pendiente
                        )

                    if (!confirmado) {
                        Log.w(
                            TAG,
                            "Revocación no confirmada; " +
                                    "se conserva para reintento"
                        )

                        return@withLock Result.retry()
                    }

                    // Eliminar solo después de HTTP 200.
                    AuthTokenManager.eliminarLogoutPendiente(
                        pendiente.solicitudId
                    )

                    Log.d(
                        TAG,
                        "Revocación confirmada y eliminada"
                    )
                }

                Result.success()
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            // Incluye errores de red, almacenamiento
            // y firma. El pendiente se conserva.
            Log.e(
                TAG,
                "No se pudo procesar logout pendiente",
                e
            )

            Result.retry()
        }
    }
}
