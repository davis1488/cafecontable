package com.ethandev.cafecontable.data.local.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
    }

    val usuarioSesion: Flow<UsuarioSesion?> =
        context.sessionDataStore.data.map { preferences ->

            val usuarioId = preferences[USUARIO_ID]
            val empresaId = preferences[EMPRESA_ID]

            if (usuarioId == null || empresaId == null) {
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
        }
    }

    suspend fun cerrarSesion() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
