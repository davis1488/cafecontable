package com.ethandev.cafecontable.domain.repository

import com.ethandev.cafecontable.domain.model.RegistrarMezclaInput

interface MezclaRepository {
    suspend fun registrarMezcla(input: RegistrarMezclaInput)
}