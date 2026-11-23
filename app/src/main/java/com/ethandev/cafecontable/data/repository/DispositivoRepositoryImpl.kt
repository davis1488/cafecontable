
package com.ethandev.cafecontable.data.repository

import android.content.Context
import android.util.Base64
import com.ethandev.cafecontable.data.remote.dispositivo.DispositivoApi
import com.ethandev.cafecontable.data.remote.dispositivo.RefreshFirmadoRequestDto
import com.ethandev.cafecontable.data.remote.dispositivo.RenovacionPendienteDto
import com.ethandev.cafecontable.data.remote.dispositivo.RegistrarDispositivoRequestDto
import com.ethandev.cafecontable.data.remote.dispositivo.SolicitarDesafioRenovacionDto
import com.ethandev.cafecontable.data.remote.dispositivo.VincularSesionDispositivoRequestDto
import com.ethandev.cafecontable.data.security.DispositivoKeyStoreManager
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import com.ethandev.cafecontable.data.remote.auth.AuthTokenManager
import com.ethandev.cafecontable.data.remote.auth.AuthTokenManager.tokenVigente

import com.ethandev.cafecontable.data.remote.auth.LoginResponseDto
import com.ethandev.cafecontable.data.remote.auth.SesionOnlineManager
import com.ethandev.cafecontable.data.remote.dispositivo.CerrarSesionFirmadoRequestDto
import com.ethandev.cafecontable.data.remote.dispositivo.CerrarSesionRequestDto
import com.ethandev.cafecontable.data.remote.dispositivo.LogoutPendienteDto
import org.json.JSONObject

class DispositivoRepositoryImpl(
    private val api: DispositivoApi,
    private val keyStore: DispositivoKeyStoreManager
) {

    // Se comparte entre instancias de este repositorio en el mismo proceso.
    // Evita rotaciones simultaneas desde ViewModels y Workers.
    private companion object {
        val renovacionMutex = Mutex()
    }

    suspend fun registrar(
        accessToken: String,
        usuarioId: String,

        ): String {

        val desafioResponse = api.solicitarDesafio(
            "Bearer $accessToken"
        )

        if (!desafioResponse.isSuccessful) {
            error("No fue posible solicitar el desafio")
        }

        val desafio = desafioResponse.body()
            ?: error("Respuesta de desafio vacia")

        val firma = keyStore.firmarRegistro(
            desafioId = UUID.fromString(desafio.desafioId),
            usuarioId = UUID.fromString(usuarioId),
            desafio = desafio.desafio
        )

        val request = RegistrarDispositivoRequestDto(
            desafioId = desafio.desafioId,
            desafio = desafio.desafio,
            clavePublica = keyStore.obtenerClavePublicaBase64(),
            firma = firma,
            nombreDispositivo = "CafeContable Android"
        )

        val response = api.registrarDispositivo(
            "Bearer $accessToken",
            request
        )

        if (!response.isSuccessful) {
            error(
                "Registro de dispositivo rechazado: HTTP ${response.code()}"
            )
        }

        return response.body()?.dispositivoId
            ?: error("No se recibio dispositivoId")
    }

    suspend fun vincular(
        accessToken: String,
        dispositivoId: String,
        usuarioId: String,
        refreshToken: String
    ) {

        val desafioResponse = api.solicitarDesafio(
            "Bearer $accessToken"
        )

        if (!desafioResponse.isSuccessful) {
            error("No fue posible solicitar el desafio")
        }

        val desafio = desafioResponse.body()
            ?: error("Respuesta de desafio vacia")

        val firma = keyStore.firmarVinculacion(
            desafioId = UUID.fromString(desafio.desafioId),
            usuarioId = UUID.fromString(usuarioId),
            dispositivoId = UUID.fromString(dispositivoId),
            refreshToken = refreshToken,
            desafio = desafio.desafio
        )

        val request = VincularSesionDispositivoRequestDto(
            dispositivoId = dispositivoId,
            desafioId = desafio.desafioId,
            desafio = desafio.desafio,
            refreshToken = refreshToken,
            firma = firma
        )

        val response = api.vincularSesion(
            "Bearer $accessToken",
            request
        )

        if (!response.isSuccessful) {
            error(
                "Vinculacion rechazada: HTTP ${response.code()}"

            )
        }

        AuthTokenManager.guardarDispositivoId(dispositivoId)

    }



    suspend fun renovarSesion(
        dispositivoId: String,
        refreshToken: String
    ): LoginResponseDto = renovacionMutex.withLock {
        require(dispositivoId.isNotBlank() && refreshToken.isNotBlank())

        // Si el proceso se cerro o la respuesta HTTP se perdio, no
        // iniciamos una nueva rotacion mientras exista una pendiente.
        val pendiente = AuthTokenManager.obtenerRenovacionPendiente()
        if (pendiente != null) {
            check(pendiente.dispositivoId.equals(dispositivoId, ignoreCase = true)) {
                "Hay una renovacion pendiente de otro dispositivo"
            }
            check(pendiente.refreshTokenAnterior == refreshToken) {
                "Hay una renovacion pendiente de otra sesion; " +
                        "debe resolverse antes de renovar"
            }
            return@withLock recuperarRenovacionPendienteInterna()
        }

        // El desafio se obtiene sin JWT; un fallo aqui no inicia la rotacion.
        val desafioResponse = api.solicitarDesafioRenovacion(
            SolicitarDesafioRenovacionDto(
                refreshToken = refreshToken,
                dispositivoId = dispositivoId
            )
        )
        check(desafioResponse.isSuccessful) {
            "Desafio de renovacion rechazado: HTTP ${desafioResponse.code()}"
        }
        val desafio = desafioResponse.body()
            ?: error("Respuesta de desafio vacia")

        val solicitudId = UUID.randomUUID()
        val firma = keyStore.firmarRenovacion(
            solicitudId = solicitudId,
            desafioId = UUID.fromString(desafio.desafioId),
            dispositivoId = UUID.fromString(dispositivoId),
            refreshToken = refreshToken,
            desafio = desafio.desafio
        )
        val request = RefreshFirmadoRequestDto(
            solicitudId = solicitudId.toString(),
            desafioId = desafio.desafioId,
            dispositivoId = dispositivoId,
            refreshToken = refreshToken,
            desafio = desafio.desafio,
            firma = firma
        )

        // Guardar ANTES de transmitir. Si el servidor procesa la solicitud
        // pero perdemos la respuesta, conservamos el UUID y token original.
        AuthTokenManager.guardarRenovacionPendiente(
            RenovacionPendienteDto(
                solicitudId = solicitudId.toString(),
                dispositivoId = dispositivoId,
                refreshTokenAnterior = refreshToken
            )
        )

        // Ni un 401 ni un error de red autorizan una segunda rotacion.
        // Se conserva el pendiente para intentar recuperacion firmada.
        val response = try {
            api.renovarSesionFirmada(request)
        } catch (e: IOException) {
            // La solicitud pudo ser confirmada por SQL Server.
            // Intentar recuperar una sola vez, sin reenviar el POST original.
            try {
                return@withLock recuperarRenovacionPendienteInterna()
            } catch (recuperacionError: Exception) {
                e.addSuppressed(recuperacionError)
                throw e
            }
        }

        check(response.isSuccessful) {
            "Renovacion rechazada: HTTP ${response.code()}. " +
                    "Se conserva la solicitud pendiente para verificar su estado."
        }


//        if (com.ethandev.cafecontable.BuildConfig.DEBUG) {
//            android.util.Log.d(
//                "CafeContableSeguridad",
//                "TEST: simulando perdida de respuesta HTTP 200"
//            )
//
//            return@withLock recuperarRenovacionPendienteInterna()
//        }


        val resultado = response.body()
            ?: error("Respuesta de renovacion vacia; pendiente conservada")
        val nuevoRefresh = resultado.refreshToken
            ?.takeIf { it.isNotBlank() }
            ?: error("Falta refresh token; pendiente conservada")
        require(resultado.accessToken.isNotBlank()) {
            "Falta access token; pendiente conservada"
        }

        AuthTokenManager.confirmarRenovacion(
            nuevoAccessToken = resultado.accessToken,
            nuevoRefreshToken = nuevoRefresh
        )
        resultado
    }




    suspend fun prepararDispositivo(
        accessToken: String,
        usuarioId: String,
        empresaId: String,
        refreshToken: String
    ): String {


        require(
            AuthTokenManager.tokenCorrespondeASesion(
                token = accessToken,
                usuarioId = usuarioId,
                empresaId = empresaId
            )
        ) {
            "El JWT no corresponde al usuario o empresa autenticados"
        }

        require(accessToken.isNotBlank()) {
            "Access token requerido"
        }

        require(refreshToken.isNotBlank()) {
            "Refresh token requerido"
        }

        // Buscar un dispositivo previamente vinculado.
        val dispositivoGuardado =
            AuthTokenManager.obtenerDispositivoId()

        // Registrar solamente si es la primera vez.
        val dispositivoId = if (dispositivoGuardado != null) {
            dispositivoGuardado
        } else {
            registrar(
                accessToken = accessToken,
                usuarioId = usuarioId
            )
        }

        // Vincular la nueva sesión al dispositivo.
        vincular(
            accessToken = accessToken,
            dispositivoId = dispositivoId,
            usuarioId = usuarioId,
            refreshToken = refreshToken
        )

        return dispositivoId
    }


    suspend fun recuperarRenovacionPendiente(): LoginResponseDto =
        renovacionMutex.withLock {
            recuperarRenovacionPendienteInterna()
        }

    // Solo invocar bajo renovacionMutex, para no anidar el mismo Mutex.
    private suspend fun recuperarRenovacionPendienteInterna(): LoginResponseDto {

        // 1. Recuperar la solicitud anterior cifrada.
        val pendiente =
            AuthTokenManager.obtenerRenovacionPendiente()
                ?: error("No existe renovación pendiente")

        // 2. Solicitar un nuevo desafío sin JWT.
        val desafioResponse = api.solicitarDesafioRenovacion(
            SolicitarDesafioRenovacionDto(
                refreshToken = pendiente.refreshTokenAnterior,
                dispositivoId = pendiente.dispositivoId
            )
        )

        if (!desafioResponse.isSuccessful) {
            error(
                "Desafío de recuperación rechazado: " +
                        "HTTP ${desafioResponse.code()}"
            )
        }

        val desafio = desafioResponse.body()
            ?: error("Desafío de recuperación vacío")

        // 3. Firmar usando el MISMO solicitudId original.
        // El desafío es nuevo.
        val firma = keyStore.firmarRenovacion(
            solicitudId = UUID.fromString(pendiente.solicitudId),
            desafioId = UUID.fromString(desafio.desafioId),
            dispositivoId = UUID.fromString(pendiente.dispositivoId),
            refreshToken = pendiente.refreshTokenAnterior,
            desafio = desafio.desafio
        )

        // 4. Reconstruir la solicitud original con
        // desafío y firma nuevos.
        val request = RefreshFirmadoRequestDto(
            solicitudId = pendiente.solicitudId,
            desafioId = desafio.desafioId,
            dispositivoId = pendiente.dispositivoId,
            refreshToken = pendiente.refreshTokenAnterior,
            desafio = desafio.desafio,
            firma = firma
        )

        // 5. Solicitar recuperación de la respuesta.
        val response = api.recuperarRenovacionFirmada(request)

        if (!response.isSuccessful) {
            error(
                "Recuperación rechazada: HTTP ${response.code()}"
            )
        }

        val resultado = response.body()
            ?: error("Respuesta de recuperación vacía")

        val nuevoRefresh = resultado.refreshToken
            ?.takeIf { it.isNotBlank() }
            ?: error("La respuesta no contiene refresh token")

        require(resultado.accessToken.isNotBlank()) {
            "La respuesta no contiene access token"
        }

        // 6. Guardar ambos tokens y eliminar el pendiente
        // en una sola escritura local.
        AuthTokenManager.confirmarRenovacion(
            nuevoAccessToken = resultado.accessToken,
            nuevoRefreshToken = nuevoRefresh
        )

        android.util.Log.d(
            "CafeContableSeguridad",
            "PASS: renovación pendiente recuperada"
        )

        return resultado
    }



    suspend fun cerrarSesionRemota(context: Context): Boolean {

        // Primero obtener un JWT vigente.
        // Este proceso puede renovar el refresh token.
        val jwt = SesionOnlineManager.obtenerJwtVigente(
            context.applicationContext
        ) ?: return false

        // Leer las credenciales DESPUÉS de la renovación.
        val refreshToken = AuthTokenManager.obtenerRefreshToken()
            ?: return false

        val dispositivoId = AuthTokenManager.obtenerDispositivoId()
            ?: return false

        val respuesta = api.cerrarSesionRemota(
            token = "Bearer $jwt",
            request = CerrarSesionRequestDto(
                refreshToken = refreshToken,
                dispositivoId = dispositivoId
            )
        )

        return respuesta.isSuccessful
    }


    // Procesa una revocación guardada, incluso sin JWT.
    // No utiliza ni modifica la sesión actualmente activa.
    suspend fun procesarLogoutPendiente(
        pendiente: LogoutPendienteDto
    ): Boolean {

        val dispositivoId = UUID.fromString(
            pendiente.dispositivoId
        )

        val solicitudId = UUID.fromString(
            pendiente.solicitudId
        )

        // 1. Solicitar desafío sin JWT.
        val respuestaDesafio =
            api.solicitarDesafioLogout(
                SolicitarDesafioRenovacionDto(
                    refreshToken = pendiente.refreshToken,
                    dispositivoId = pendiente.dispositivoId
                )
            )

        if (!respuestaDesafio.isSuccessful) {
            android.util.Log.w(
                "CafeContableSeguridad",
                "Desafío de logout rechazado: " +
                        "HTTP ${respuestaDesafio.code()}"
            )
            return false
        }

        val desafio = respuestaDesafio.body()
            ?: return false

        // 2. Firmar usando la clave privada de AndroidKeyStore.
        val firma = keyStore.firmarLogout(
            solicitudId = solicitudId,
            desafioId = UUID.fromString(desafio.desafioId),
            dispositivoId = dispositivoId,
            refreshToken = pendiente.refreshToken,
            desafio = desafio.desafio
        )

        // 3. Ejecutar logout firmado.
        val respuesta = api.cerrarSesionFirmada(
            CerrarSesionFirmadoRequestDto(
                solicitudId = pendiente.solicitudId,
                dispositivoId = pendiente.dispositivoId,
                desafioId = desafio.desafioId,
                refreshToken = pendiente.refreshToken,
                desafio = desafio.desafio,
                firma = firma
            )
        )

        // HTTP 200: el servidor confirmó la revocación
        // o confirmó que la sesión ya estaba revocada.
        return respuesta.isSuccessful
    }





}
