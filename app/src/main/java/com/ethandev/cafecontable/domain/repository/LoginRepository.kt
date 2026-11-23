
package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.data.remote.auth.LoginResponseDto

interface LoginRepository {

    suspend fun iniciarSesion(
        correo: String,
        password: String
    ): Result<LoginResponseDto>
}
