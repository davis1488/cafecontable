
package com.ethandev.cafecontable.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(
    name = "cafecontable_session"
)

data class UsuarioSesion(
    val usuarioId: String,
    val empresaId: String,
    val nombre: String,
    val rol: String
)

class SessionManager(
    private val context: Context
) {

    companion object {
        private val USUARIO_ID = stringPreferencesKey("usuario_id")
        private val EMPRESA_ID = stringPreferencesKey("empresa_id")
        private val NOMBRE = stringPreferencesKey("nombre")
        private val ROL = stringPreferencesKey("rol")

        private val OFFLINE_HASTA =
            longPreferencesKey("offline_hasta")

        private const val DURACION_OFFLINE_MS =
            7L * 24 * 60 * 60 * 1000L
    }

    val usuarioSesion: Flow<UsuarioSesion?> =
        context.sessionDataStore.data.map { preferences ->

            val usuarioId = preferences[USUARIO_ID]
            val empresaId = preferences[EMPRESA_ID]

            if (usuarioId.isNullOrBlank() ||
                empresaId.isNullOrBlank()
            ) {
                null
            } else {
                UsuarioSesion(
                    usuarioId = usuarioId,
                    empresaId = empresaId,
                    nombre = preferences[NOMBRE].orEmpty(),
                    rol = preferences[ROL].orEmpty()
                )
            }
        }

    suspend fun guardarUsuario(
        usuarioId: String,
        empresaId: String,
        nombre: String,
        rol: String
    ) {
        context.sessionDataStore.edit { preferences ->
            preferences[USUARIO_ID] = usuarioId
            preferences[EMPRESA_ID] = empresaId
            preferences[NOMBRE] = nombre
            preferences[ROL] = rol

            // Una nueva identidad no hereda
            // una autorización offline anterior.
            preferences.remove(OFFLINE_HASTA)
        }
    }

    suspend fun habilitarAccesoOffline() {
        context.sessionDataStore.edit { preferences ->

            val usuarioId = preferences[USUARIO_ID]
            val empresaId = preferences[EMPRESA_ID]

            require(
                !usuarioId.isNullOrBlank() &&
                        !empresaId.isNullOrBlank()
            ) {
                "No existe un usuario autenticado"
            }

            preferences[OFFLINE_HASTA] =
                System.currentTimeMillis() +
                        DURACION_OFFLINE_MS
        }
    }

    suspend fun obtenerSesionOfflineValida(): UsuarioSesion? {

        val usuario = usuarioSesion.first()
            ?: return null

        val preferences = context.sessionDataStore.data.first()

        val vencimiento = preferences[OFFLINE_HASTA]
            ?: return null

        val ahora = System.currentTimeMillis()

        if (ahora >= vencimiento) {
            return null
        }

        return usuario
    }

//prueba
    suspend fun simularVencimientoOfflineParaPrueba() {

        // Permitir esta operación únicamente en compilaciones
        // Android marcadas como depurables.
        val esDebuggable =
            (context.applicationInfo.flags and
                    android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        check(esDebuggable) {
            "Esta prueba solo está permitida en modo Debug"
        }

        context.sessionDataStore.edit { preferences ->
            preferences[OFFLINE_HASTA] =
                System.currentTimeMillis() - 60_000L
        }
    }
///////////

    suspend fun cerrarSesion() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }


    suspend fun guardarSesionAutorizada(
        usuarioId: String,
        empresaId: String,
        nombre: String,
        rol: String
    ) {
        require(usuarioId.isNotBlank()) {
            "usuarioId es obligatorio"
        }

        require(empresaId.isNotBlank()) {
            "empresaId es obligatorio"
        }

        val offlineHasta =
            System.currentTimeMillis() + DURACION_OFFLINE_MS

        // Una sola transacción de DataStore.
        context.sessionDataStore.edit { preferences ->
            preferences[USUARIO_ID] = usuarioId
            preferences[EMPRESA_ID] = empresaId
            preferences[NOMBRE] = nombre
            preferences[ROL] = rol
            preferences[OFFLINE_HASTA] = offlineHasta
        }
    }







}
