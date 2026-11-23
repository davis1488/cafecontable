
package com.ethandev.cafecontable.data.remote.auth

import android.content.Context
import com.ethandev.cafecontable.core.AppModule
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import android.util.Log
import com.ethandev.cafecontable.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

object SesionOnlineManager {

    private val mutex = Mutex()

//    private val forzarRenovacionPrueba = AtomicBoolean(true)

    suspend fun obtenerJwtVigente(
        context: Context
    ): String? = mutex.withLock {

        AuthTokenManager.inicializar(
            context.applicationContext
        )


        if (AuthTokenManager.existeLoginIncompleto()) {
            Log.w(
                "CafeContableSeguridad",
                "P20.12: login incompleto; " +
                        "se bloquea la obtención y renovación del JWT"
            )

            return@withLock null
        }


        // 1. Revisar si existe una renovación pendiente.
        val pendiente =
            AuthTokenManager.obtenerRenovacionPendiente()

        if (pendiente != null) {
            val repository =
                AppModule.provideDispositivoRepository()

            // Recupera y guarda las credenciales.
            repository.recuperarRenovacionPendiente()

            val recuperado =
                AuthTokenManager.obtenerToken()

            return@withLock recuperado?.takeIf {
                AuthTokenManager.tokenVigente(it)
            }
        }

        // 2. Consultar el JWT actual o restaurarlo.
        val token = AuthTokenManager.obtenerToken()
            ?: AuthTokenManager.restaurarToken()

        if (token != null &&
            AuthTokenManager.tokenVigente(token)) {
            return@withLock token
        }



//        ///////////////////////pru
//
//        val forzarRenovacion =
//            BuildConfig.DEBUG &&
//                    forzarRenovacionPrueba.compareAndSet(true, false)
//
//        if (forzarRenovacion) {
//            Log.d(
//                "CafeContableSeguridad",
//                "TEST P20.8: forzando renovación desde WorkManager"
//            )
//        }
//
//        if (!forzarRenovacion &&
//            token != null &&
//            AuthTokenManager.tokenVigente(token)) {
//            return@withLock token
//        }
//
//        ////////////////////////////////pru

        // 3. No hay JWT vigente: buscar credenciales.
        val refreshToken =
            AuthTokenManager.obtenerRefreshToken()
                ?: return@withLock null

        val dispositivoId =
            AuthTokenManager.obtenerDispositivoId()
                ?: return@withLock null

        // 4. Renovar utilizando firma ECDSA.
        val repository =
            AppModule.provideDispositivoRepository()

        repository.renovarSesion(
            dispositivoId = dispositivoId,
            refreshToken = refreshToken
        )

        // 5. El repositorio guardó los tokens nuevos.
        val nuevoToken =
            AuthTokenManager.obtenerToken()

        return@withLock nuevoToken?.takeIf {
            AuthTokenManager.tokenVigente(it)
        }
    }
}
