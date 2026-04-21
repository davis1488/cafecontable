package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.repository.dto.RegistrarPreparacionEntregaInput

interface PreparacionEntregaRepository {
    suspend fun registrarPreparacion(input: RegistrarPreparacionEntregaInput)
}