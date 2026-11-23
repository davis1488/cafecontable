
package com.ethandev.cafecontable.domain.usecase

import android.content.Context
import android.util.Log
import com.ethandev.cafecontable.data.local.session.SessionManager
import com.ethandev.cafecontable.data.remote.auth.AuthTokenManager
import com.ethandev.cafecontable.data.remote.auth.CoordinadorSesion
import com.ethandev.cafecontable.data.remote.dispositivo.LogoutPendienteDto
import com.ethandev.cafecontable.data.repository.DispositivoRepositoryImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import com.ethandev.cafecontable.data.worker.ProgramadorLogoutPendiente
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext



class CerrarSesionUseCase(
    private val sessionManager: SessionManager,
    private val dispositivoRepository: DispositivoRepositoryImpl,
    private val context: Context
) {


    suspend operator fun invoke() {
        CoordinadorSesion.mutex.withLock {

            // 1. Obtener credenciales antes de cerrar sesión.
            // Si hay una renovación pendiente, el token anterior
            // sigue identificando la misma sesión en el backend.
            val renovacionPendiente =
                AuthTokenManager.obtenerRenovacionPendiente()

            val refreshToken =
                renovacionPendiente?.refreshTokenAnterior
                    ?: AuthTokenManager.obtenerRefreshToken()

            val dispositivoId =
                AuthTokenManager.obtenerDispositivoId()

            // 2. No borrar credenciales si falta información
            // necesaria para revocar una sesión conocida.
            check(
                (refreshToken == null && dispositivoId == null) ||
                        (refreshToken != null && dispositivoId != null)
            ) {
                "No se puede cerrar sesión: " +
                        "credenciales del dispositivo incompletas"
            }

            // 3. Guardar primero la revocación cifrada.
            if (refreshToken != null && dispositivoId != null) {

                val pendiente = LogoutPendienteDto(
                    solicitudId = UUID.randomUUID().toString(),
                    dispositivoId = dispositivoId,
                    refreshToken = refreshToken
                )

                AuthTokenManager.guardarLogoutPendiente(pendiente)

                Log.d(
                    "CafeContableSeguridad",
                    "Revocación pendiente guardada correctamente"
                )
            }

            // 4. Una vez registrada la revocación, completar
            // el cierre local aunque se cancele la corrutina.
            withContext(NonCancellable) {

                try {
                    sessionManager.cerrarSesion()
                } finally {
                    AuthTokenManager.limpiarToken()
                }

                Log.d(
                    "CafeContableSeguridad",
                    "Sesión local cerrada"
                )

                // 5. Programar el logout firmado.
                // Un error al programar no elimina el pendiente.

                try {
                    if (AuthTokenManager
                            .obtenerLogoutsPendientes()
                            .isNotEmpty()
                    ) {
                        ProgramadorLogoutPendiente.programar(
                            context.applicationContext
                        )

                        Log.d(
                            "CafeContableSeguridad",
                            "Worker de logout programado"
                        )
                    }

                    Unit // Evita que el if sea la última expresión del try.

                } catch (e: Exception) {
                    Log.e(
                        "CafeContableSeguridad",
                        "No se pudo programar logout; " +
                                "revocaciones conservadas",
                        e
                    )
                }

            }
        }
    }
}
