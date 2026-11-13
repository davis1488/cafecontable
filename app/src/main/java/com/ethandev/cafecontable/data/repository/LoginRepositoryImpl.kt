
package com.ethandev.cafecontable.data.repository

import com.ethandev.cafecontable.data.remote.LoginRequestDto
import com.ethandev.cafecontable.data.remote.LoginResponseDto
import com.ethandev.cafecontable.data.remote.RetrofitClient
import com.ethandev.cafecontable.domain.repository.LoginRepository

class LoginRepositoryImpl : LoginRepository {
    override suspend fun iniciarSesion(
        correo: String,
        password: String
    ): Result<LoginResponseDto> {

        return try {
            val response = RetrofitClient.api.login(
                LoginRequestDto(
                    correo = correo,
                    password = password
                )
            )

            if (response.isSuccessful) {
                val usuario = response.body()

                if (usuario != null) {
                    Result.success(usuario)
                } else {
                    Result.failure(
                        Exception("Respuesta vacía del servidor")
                    )
                }
            } else {
                Result.failure(
                    Exception(
                        when (response.code()) {
                            401 -> "Correo o contraseña incorrectos"
                            403 -> "Usuario sin autorización"
                            else -> "Error del servidor: ${response.code()}"
                        }
                    )
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
