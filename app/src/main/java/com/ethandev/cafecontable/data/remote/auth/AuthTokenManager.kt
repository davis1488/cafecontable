
package com.ethandev.cafecontable.data.remote.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import com.ethandev.cafecontable.data.remote.dispositivo.RenovacionPendienteDto
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONArray
import com.ethandev.cafecontable.data.remote.dispositivo.LogoutPendienteDto


object AuthTokenManager {

    private const val KEY_ALIAS = "cafecontable_jwt_key"
    private const val PREFS = "cafecontable_auth_segura"
    private const val TOKEN = "jwt_cifrado"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val REFRESH_TOKEN = "refresh_token_cifrado"
    private const val DISPOSITIVO_ID = "dispositivo_id_cifrado"
    private const val RENOVACION_PENDIENTE = "renovacion_pendiente_cifrada"
    private const val LOGOUTS_PENDIENTES =
        "logouts_pendientes_cifrados"

    @Volatile
    private var accessToken: String? = null

    private lateinit var appContext: Context

    fun inicializar(context: Context) {
        appContext = context.applicationContext
    }

    private fun preferences() =
        appContext.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

    private fun obtenerClave(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val existente = keyStore.getKey(
            KEY_ALIAS,
            null
        ) as? SecretKey

        if (existente != null) return existente

        val generador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        generador.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .setKeySize(256)
                .build()
        )

        return generador.generateKey()
    }

    fun guardarToken(token: String) {
        check(AuthTokenManager::appContext.isInitialized) {
            "AuthTokenManager no está inicializado"
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            obtenerClave()
        )

        val cifrado = cipher.doFinal(
            token.toByteArray(Charsets.UTF_8)
        )

        val contenido = cipher.iv + cifrado

        val texto = Base64.encodeToString(
            contenido,
            Base64.NO_WRAP
        )

        check(
            preferences().edit()
                .putString(TOKEN, texto)
                .commit()
        ) {
            "No se pudo guardar el token"
        }

        accessToken = token
    }

    fun obtenerToken(): String? = accessToken

    fun restaurarToken(): String? {
        if (!AuthTokenManager::appContext.isInitialized) return null

        val texto = preferences().getString(
            TOKEN,
            null
        ) ?: return null

        return try {
            val datos = Base64.decode(
                texto,
                Base64.DEFAULT
            )

            require(datos.size > 12)

            val iv = datos.copyOfRange(0, 12)
            val cifrado = datos.copyOfRange(
                12,
                datos.size
            )

            val cipher = Cipher.getInstance(TRANSFORMATION)

            cipher.init(
                Cipher.DECRYPT_MODE,
                obtenerClave(),
                GCMParameterSpec(128, iv)
            )

            val token = String(
                cipher.doFinal(cifrado),
                Charsets.UTF_8
            )

            if (!tokenVigente(token)) {
                limpiarAccessToken()
                return null
            }

            accessToken = token
            token

        } catch (e: Exception) {
            limpiarAccessToken()
            null
        }
    }

    fun tokenVigente(token: String): Boolean {
        return try {
            val partes = token.split(".")
            if (partes.size != 3) return false

            val payload = String(
                Base64.decode(
                    partes[1],
                    Base64.URL_SAFE or
                            Base64.NO_WRAP or
                            Base64.NO_PADDING
                ),
                Charsets.UTF_8
            )

            val exp = JSONObject(payload).getLong("exp")
            val ahora = System.currentTimeMillis() / 1000

            exp > ahora + 30

        } catch (e: Exception) {
            false
        }
    }

    // El JWT puede vencer sin invalidar el refresh token ni el acceso offline.
    fun limpiarAccessToken() {
        accessToken = null

        if (::appContext.isInitialized) {
            check(preferences().edit().remove(TOKEN).commit()) {
                "No se pudo eliminar el access token"
            }
        }
    }

    // Cierre de sesion: eliminar todas las credenciales locales, incluido el dispositivo.
    fun limpiarToken() {
        accessToken = null

        if (::appContext.isInitialized) {
            check(
                preferences().edit()
                    .remove(TOKEN)
                    .remove(REFRESH_TOKEN)
                    .remove(DISPOSITIVO_ID)
                    .remove(RENOVACION_PENDIENTE)
                    .commit()
            ) {
                "No se pudieron eliminar las credenciales locales"
            }
        }
    }


    fun guardarRefreshToken(token: String) {
        check(::appContext.isInitialized) {
            "AuthTokenManager no está inicializado"
        }

        require(token.isNotBlank())

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            obtenerClave()
        )

        val contenido = cipher.iv +
                cipher.doFinal(token.toByteArray(Charsets.UTF_8))

        val cifrado = Base64.encodeToString(
            contenido,
            Base64.NO_WRAP
        )

        check(
            preferences().edit()
                .putString(REFRESH_TOKEN, cifrado)
                .commit()
        ) {
            "No se pudo guardar el refresh token"
        }
    }

    // El identificador NO es una credencial secreta, pero lo ciframos para
    // almacenarlo junto a la informacion de autenticacion del dispositivo.
    fun guardarDispositivoId(dispositivoId: String) {
        check(::appContext.isInitialized) {
            "AuthTokenManager no esta inicializado"
        }

        require(dispositivoId.isNotBlank()) {
            "dispositivoId es obligatorio"
        }
        require(runCatching { java.util.UUID.fromString(dispositivoId) }.isSuccess) {
            "dispositivoId debe ser un UUID valido"
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, obtenerClave())

        val contenido = cipher.iv +
                cipher.doFinal(dispositivoId.toByteArray(Charsets.UTF_8))
        val cifrado = Base64.encodeToString(contenido, Base64.NO_WRAP)

        check(preferences().edit().putString(DISPOSITIVO_ID, cifrado).commit()) {
            "No se pudo guardar dispositivoId"
        }
    }

    fun obtenerDispositivoId(): String? {
        if (!::appContext.isInitialized) return null
        val texto = preferences().getString(DISPOSITIVO_ID, null) ?: return null

        return try {
            val datos = Base64.decode(texto, Base64.DEFAULT)
            require(datos.size > 28)

            val iv = datos.copyOfRange(0, 12)
            val cifrado = datos.copyOfRange(12, datos.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, obtenerClave(), GCMParameterSpec(128, iv))

            val id = String(cipher.doFinal(cifrado), Charsets.UTF_8)
            java.util.UUID.fromString(id)
            id
        } catch (e: Exception) {
            preferences().edit().remove(DISPOSITIVO_ID).commit()
            null
        }
    }

    fun obtenerRefreshToken(): String? {
        if (!::appContext.isInitialized) return null

        val texto = preferences()
            .getString(REFRESH_TOKEN, null)
            ?: return null

        return try {
            val datos = Base64.decode(
                texto,
                Base64.DEFAULT
            )

            require(datos.size > 28)

            val iv = datos.copyOfRange(0, 12)
            val cifrado = datos.copyOfRange(
                12,
                datos.size
            )

            val cipher = Cipher.getInstance(TRANSFORMATION)

            cipher.init(
                Cipher.DECRYPT_MODE,
                obtenerClave(),
                GCMParameterSpec(128, iv)
            )

            String(
                cipher.doFinal(cifrado),
                Charsets.UTF_8
            )
        } catch (e: Exception) {
            preferences().edit()
                .remove(REFRESH_TOKEN)
                .commit()
            null
        }
    }

    // Cifra un texto con la misma clave AES-GCM del Android Keystore.
    // Se genera un IV nuevo en cada operacion.
    private fun cifrarTexto(valor: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, obtenerClave())
        val contenido = cipher.iv +
                cipher.doFinal(valor.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(contenido, Base64.NO_WRAP)
    }

    private fun descifrarTexto(texto: String): String {
        val datos = Base64.decode(texto, Base64.DEFAULT)
        require(datos.size > 28) { "Contenido cifrado invalido" }
        val iv = datos.copyOfRange(0, 12)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            obtenerClave(),
            GCMParameterSpec(128, iv)
        )
        return String(
            cipher.doFinal(datos.copyOfRange(12, datos.size)),
            Charsets.UTF_8
        )
    }

    // Guardar ANTES de enviar POST /api/auth/refresh-firmado.
    @Synchronized
    fun guardarRenovacionPendiente(pendiente: RenovacionPendienteDto) {
        check(::appContext.isInitialized) {
            "AuthTokenManager no esta inicializado"
        }
        require(runCatching { java.util.UUID.fromString(pendiente.solicitudId) }.isSuccess)
        require(runCatching { java.util.UUID.fromString(pendiente.dispositivoId) }.isSuccess)
        require(pendiente.refreshTokenAnterior.isNotBlank())

        // No se permite reemplazar una renovacion que podria estar confirmada
        // en el servidor. Primero debe resolverse o descartarse explicitamente.
        check(!preferences().contains(RENOVACION_PENDIENTE)) {
            "Existe una renovacion pendiente por resolver"
        }

        val json = JSONObject()
            .put("solicitudId", pendiente.solicitudId)
            .put("dispositivoId", pendiente.dispositivoId)
            .put("refreshTokenAnterior", pendiente.refreshTokenAnterior)
            .toString()

        check(
            preferences().edit()
                .putString(RENOVACION_PENDIENTE, cifrarTexto(json))
                .commit()
        ) { "No se pudo guardar la renovacion pendiente" }
    }

    @Synchronized
    fun obtenerRenovacionPendiente(): RenovacionPendienteDto? {
        if (!::appContext.isInitialized) return null
        val texto = preferences().getString(RENOVACION_PENDIENTE, null)
            ?: return null

        // Fallar explicitamente si la informacion no se puede recuperar;
        // NO borrar el dato, ni iniciar otra rotacion silenciosamente.
        val json = JSONObject(descifrarTexto(texto))
        return RenovacionPendienteDto(
            solicitudId = json.getString("solicitudId"),
            dispositivoId = json.getString("dispositivoId"),
            refreshTokenAnterior = json.getString("refreshTokenAnterior")
        )
    }

    // Graba el nuevo par y elimina el pendiente en una sola transaccion
    // de SharedPreferences. Ejecutar tras recibir HTTP 200 y validar la respuesta.
    @Synchronized
    fun confirmarRenovacion(
        nuevoAccessToken: String,
        nuevoRefreshToken: String
    ) {
        check(::appContext.isInitialized) {
            "AuthTokenManager no esta inicializado"
        }
        require(nuevoAccessToken.isNotBlank())
        require(nuevoRefreshToken.isNotBlank())
        check(preferences().contains(RENOVACION_PENDIENTE)) {
            "No existe renovacion pendiente"
        }

        val accessCifrado = cifrarTexto(nuevoAccessToken)
        val refreshCifrado = cifrarTexto(nuevoRefreshToken)

        check(
            preferences().edit()
                .putString(TOKEN, accessCifrado)
                .putString(REFRESH_TOKEN, refreshCifrado)
                .remove(RENOVACION_PENDIENTE)
                .commit()
        ) { "No se pudieron confirmar las nuevas credenciales" }

        accessToken = nuevoAccessToken
    }


    @Synchronized
    fun guardarLogoutPendiente(pendiente: LogoutPendienteDto) {
        check(::appContext.isInitialized)

        UUIDValidar(pendiente.solicitudId)
        UUIDValidar(pendiente.dispositivoId)
        require(pendiente.refreshToken.isNotBlank())

        val lista = leerLogoutsPendientesInterno()

        check(lista.none { it.solicitudId == pendiente.solicitudId }) {
            "La solicitud de logout ya existe"
        }

        lista.add(pendiente)
        guardarListaLogouts(lista)
    }

    @Synchronized
    fun obtenerLogoutsPendientes(): List<LogoutPendienteDto> =
        leerLogoutsPendientesInterno().toList()

    @Synchronized
    fun eliminarLogoutPendiente(solicitudId: String) {
        UUIDValidar(solicitudId)

        val lista = leerLogoutsPendientesInterno()
        val restantes = lista.filterNot {
            it.solicitudId == solicitudId
        }

        if (restantes.size == lista.size) return

        guardarListaLogouts(restantes)
    }

    private fun UUIDValidar(valor: String) {
        require(
            runCatching {
                java.util.UUID.fromString(valor)
            }.isSuccess
        ) { "UUID invalido" }
    }

    private fun leerLogoutsPendientesInterno():
            MutableList<LogoutPendienteDto> {

        val texto = preferences()
            .getString(LOGOUTS_PENDIENTES, null)
            ?: return mutableListOf()

        // Si falla el descifrado, conservar el dato cifrado.
        // Nunca reemplazarlo silenciosamente.
        val array = JSONArray(descifrarTexto(texto))
        val lista = mutableListOf<LogoutPendienteDto>()

        for (i in 0 until array.length()) {
            val json = array.getJSONObject(i)

            lista.add(
                LogoutPendienteDto(
                    solicitudId = json.getString("solicitudId"),
                    dispositivoId = json.getString("dispositivoId"),
                    refreshToken = json.getString("refreshToken")
                )
            )
        }

        return lista
    }

    private fun guardarListaLogouts(
        lista: List<LogoutPendienteDto>
    ) {
        val array = JSONArray()

        lista.forEach { pendiente ->
            array.put(
                JSONObject()
                    .put("solicitudId", pendiente.solicitudId)
                    .put("dispositivoId", pendiente.dispositivoId)
                    .put("refreshToken", pendiente.refreshToken)
            )
        }

        val cifrado = cifrarTexto(array.toString())

        check(
            preferences().edit()
                .putString(LOGOUTS_PENDIENTES, cifrado)
                .commit()
        ) {
            "No se pudieron guardar los logouts pendientes"
        }
    }


    @Synchronized
    fun guardarCredencialesAtomicas(
        nuevoAccessToken: String,
        nuevoRefreshToken: String
    ) {
        check(::appContext.isInitialized) {
            "AuthTokenManager no está inicializado"
        }

        require(nuevoAccessToken.isNotBlank()) {
            "Access token obligatorio"
        }

        require(nuevoRefreshToken.isNotBlank()) {
            "Refresh token obligatorio"
        }

        require(tokenVigente(nuevoAccessToken)) {
            "Access token vencido o inválido"
        }

        // Preparar ambos valores antes de modificar
        // las credenciales almacenadas.
        val accessCifrado = cifrarTexto(nuevoAccessToken)
        val refreshCifrado = cifrarTexto(nuevoRefreshToken)

        // Una sola escritura de SharedPreferences.
        val guardado = preferences().edit()
            .putString(TOKEN, accessCifrado)
            .putString(REFRESH_TOKEN, refreshCifrado)
            .commit()

        check(guardado) {
            "No se pudieron guardar las credenciales"
        }

        // Actualizar memoria solo después de
        // confirmar el almacenamiento.
        accessToken = nuevoAccessToken
    }


    fun tokenCorrespondeASesion(
        token: String,
        usuarioId: String,
        empresaId: String
    ): Boolean {
        return try {
            if (!tokenVigente(token)) return false

            val partes = token.split(".")
            if (partes.size != 3) return false

            val payload = String(
                Base64.decode(
                    partes[1],
                    Base64.URL_SAFE or
                            Base64.NO_WRAP or
                            Base64.NO_PADDING
                ),
                Charsets.UTF_8
            )

            val json = JSONObject(payload)

            // ClaimTypes.NameIdentifier de ASP.NET Identity.
            val claimUsuario =
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier"

            val usuarioToken = json.optString(claimUsuario)
            val empresaToken = json.optString("EmpresaId")

            usuarioToken.equals(usuarioId, ignoreCase = true) &&
                    empresaToken.equals(empresaId, ignoreCase = true)

        } catch (e: Exception) {
            false
        }
    }



}
