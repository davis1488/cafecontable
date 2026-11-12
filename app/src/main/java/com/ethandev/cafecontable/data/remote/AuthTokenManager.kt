
package com.ethandev.cafecontable.data.remote

object AuthTokenManager {

    @Volatile
    private var accessToken: String? = null

    fun guardarToken(token: String) {
        accessToken = token
    }

    fun obtenerToken(): String? {
        return accessToken
    }

    fun limpiarToken() {
        accessToken = null
    }
}
