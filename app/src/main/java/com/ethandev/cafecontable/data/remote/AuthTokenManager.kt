
package com.ethandev.cafecontable.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AuthTokenManager {

    private const val KEY_ALIAS = "cafecontable_jwt_key"
    private const val PREFS = "cafecontable_auth_segura"
    private const val TOKEN = "jwt_cifrado"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

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
        check(::appContext.isInitialized) {
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
        if (!::appContext.isInitialized) return null

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
                limpiarToken()
                return null
            }

            accessToken = token
            token

        } catch (e: Exception) {
            limpiarToken()
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

    fun limpiarToken() {
        accessToken = null

        if (::appContext.isInitialized) {
            preferences().edit()
                .remove(TOKEN)
                .commit()
        }
    }
}
