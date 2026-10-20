package com.ethandev.cafecontable.data.local

import androidx.room.TypeConverter
import com.ethandev.cafecontable.domain.constants.EstadoOperacion

class Converters {

    @TypeConverter
    fun fromEstadoOperacion(value: EstadoOperacion): String {
        return value.name
    }

    @TypeConverter
    fun toEstadoOperacion(value: String): EstadoOperacion {
        return EstadoOperacion.valueOf(value)
    }
}