
package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.data.remote.LoginResponseDto

interface LoginRepository {

    suspend fun iniciarSesion(
        correo: String,
        password: String
    ): Result<LoginResponseDto>
}
