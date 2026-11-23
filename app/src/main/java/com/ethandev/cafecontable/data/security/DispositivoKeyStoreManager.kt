
package com.ethandev.cafecontable.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.UUID

class DispositivoKeyStoreManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "cafecontable_dispositivo_ecdsa_v1"
    }

    private val keyStore: KeyStore
        get() = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

    // Crear una clave o recuperar la existente.
    fun obtenerOCrearClave(): KeyPair {

        val almacen = keyStore

        val privadaExistente = almacen.getKey(
            ALIAS,
            null
        ) as? java.security.PrivateKey

        val publicaExistente = almacen.getCertificate(
            ALIAS
        )?.publicKey

        if (privadaExistente != null &&
            publicaExistente != null
        ) {
            return KeyPair(
                publicaExistente,
                privadaExistente
            )
        }

        // ECDSA P-256 compatible con .NET.
        val generador = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val configuracion = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_SIGN
        )
            .setAlgorithmParameterSpec(
                ECGenParameterSpec("secp256r1")
            )
            .setDigests(
                KeyProperties.DIGEST_SHA256
            )
            .setUserAuthenticationRequired(false)
            .build()

        generador.initialize(configuracion)

        return generador.generateKeyPair()
    }

    // Clave pública en formato SPKI Base64.
    fun obtenerClavePublicaBase64(): String {

        val publica = obtenerOCrearClave().public

        return Base64.encodeToString(
            publica.encoded,
            Base64.NO_WRAP
        )
    }

    // SHA-256 expresado como hexadecimal mayúsculo.
    fun calcularHash(texto: String): String {

        val bytes = texto.toByteArray(
            StandardCharsets.UTF_8
        )

        val hash = MessageDigest
            .getInstance("SHA-256")
            .digest(bytes)

        return hash.joinToString("") {
            "%02X".format(it.toInt() and 0xFF)
        }
    }

    // Firma SHA256withECDSA (DER).
    fun firmar(mensaje: String): String {

        val privada = obtenerOCrearClave().private

        val firma = Signature.getInstance(
            "SHA256withECDSA"
        )

        firma.initSign(privada)

        firma.update(
            mensaje.toByteArray(
                StandardCharsets.UTF_8
            )
        )

        return Base64.encodeToString(
            firma.sign(),
            Base64.NO_WRAP
        )
    }

    private fun mensaje(vararg campos: String): String {
        return campos.joinToString("\n")
    }

    // Firma para registrar el dispositivo.
    fun firmarRegistro(
        desafioId: UUID,
        usuarioId: UUID,
        desafio: String
    ): String {

        val clavePublica =
            obtenerClavePublicaBase64()

        val hashClave = calcularHash(
            clavePublica.let {
                // Hash de los bytes SPKI, no del Base64.
                val bytes = Base64.decode(
                    it,
                    Base64.DEFAULT
                )

                return@let bytes.joinToString("") { byte ->
                    "%02X".format(byte.toInt() and 0xFF)
                }
            }
        )

        // El hash real del SPKI se calcula más abajo.
        val hashSpki = MessageDigest
            .getInstance("SHA-256")
            .digest(
                Base64.decode(
                    clavePublica,
                    Base64.DEFAULT
                )
            )
            .joinToString("") {
                "%02X".format(it.toInt() and 0xFF)
            }

        val contenido = mensaje(
            "CAFECONTABLE_REGISTRAR_DISPOSITIVO_V1",
            desafioId.toString(),
            usuarioId.toString(),
            hashSpki,
            desafio
        )

        return firmar(contenido)
    }

    // Firma para vincular sesión y dispositivo.
    fun firmarVinculacion(
        desafioId: UUID,
        usuarioId: UUID,
        dispositivoId: UUID,
        refreshToken: String,
        desafio: String
    ): String {

        return firmar(
            mensaje(
                "CAFECONTABLE_VINCULAR_SESION_V1",
                desafioId.toString(),
                usuarioId.toString(),
                dispositivoId.toString(),
                calcularHash(refreshToken),
                desafio
            )
        )
    }

    // Firma para renovar una sesión.
    fun firmarRenovacion(
        solicitudId: UUID,
        desafioId: UUID,
        dispositivoId: UUID,
        refreshToken: String,
        desafio: String
    ): String {

        return firmar(
            mensaje(
                "CAFECONTABLE_REFRESH_FIRMADO_V1",
                solicitudId.toString(),
                desafioId.toString(),
                dispositivoId.toString(),
                calcularHash(refreshToken),
                desafio
            )
        )
    }


    // P20.11: Firma de cierre de sesión sin JWT.
    fun firmarLogout(
        solicitudId: UUID,
        desafioId: UUID,
        dispositivoId: UUID,
        refreshToken: String,
        desafio: String
    ): String {

        require(refreshToken.isNotBlank())
        require(desafio.isNotBlank())

        return firmar(
            mensaje(
                "CAFECONTABLE_LOGOUT_FIRMADO_V1",
                solicitudId.toString(),
                desafioId.toString(),
                dispositivoId.toString(),
                calcularHash(refreshToken),
                desafio
            )
        )
    }




}
