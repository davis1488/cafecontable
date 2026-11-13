
package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.data.local.session.SessionManager
import com.ethandev.cafecontable.data.remote.AuthTokenManager

class CerrarSesionUseCase(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke() {
        sessionManager.cerrarSesion()
        AuthTokenManager.limpiarToken()
    }
}
