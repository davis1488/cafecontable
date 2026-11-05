
package com.ethandev.cafecontable.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FechaUtils {

    fun formatearFecha(timestamp: Long): String {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale("es", "CO")
        )

        formato.timeZone =
            TimeZone.getTimeZone("America/Bogota")

        return formato.format(Date(timestamp))
    }


    fun generarNombreOperacion(
        tipo: String,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val formato = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale("es", "CO")
        )

        formato.timeZone =
            TimeZone.getTimeZone("America/Bogota")

        return "$tipo ${formato.format(Date(timestamp))}"
    }



    fun formatearNombreOperacion(nombre: String): String {
        val timestamp = nombre.substringAfterLast(" ").toLongOrNull()
            ?: return nombre

        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale("es", "CO")
        )

        formato.timeZone =
            TimeZone.getTimeZone("America/Bogota")

        val tipo = nombre.substringBeforeLast(" ")

        return "$tipo ${formato.format(Date(timestamp))}"
    }

}
