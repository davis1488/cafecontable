package com.ethandev.cafecontable.domain.repository

interface PreparacionEntregaRepository {
    suspend fun registrarPreparacion(input: RegistrarPreparacionEntregaInput)
}