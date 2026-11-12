
package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.LoginRepository
import com.ethandev.cafecontable.data.remote.LoginResponseDto

class IniciarSesionUseCase(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(
        correo: String,
        password: String
    ): Result<LoginResponseDto> {

        if (correo.isBlank() || password.isBlank()) {
            return Result.failure(
                IllegalArgumentException(
                    "Debes ingresar correo y contraseña"
                )
            )
        }

        return repository.iniciarSesion(
            correo = correo.trim(),
            password = password
        )
    }
}
