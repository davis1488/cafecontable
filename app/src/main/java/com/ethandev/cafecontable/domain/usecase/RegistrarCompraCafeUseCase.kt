package com.ethandev.cafecontable.domain.usecase

import com.ethandev.cafecontable.domain.repository.CompraCafeImput
//import com.ethandev.cafecontable.domain.repository.CompraCafeInput
import com.ethandev.cafecontable.domain.repository.CompraRepository

class RegistrarCompraCafeUseCase(private val repo: CompraRepository) {
    suspend operator fun invoke(input: CompraCafeImput) {
        repo.registrarCompra(input)
    }



}