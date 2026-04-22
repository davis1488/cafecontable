package com.ethandev.cafecontable.ui.utils

import java.text.NumberFormat
import java.util.Locale

fun formatNumber(value: String): String {
    return value
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

fun formatNumberString(value: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
    return formatter.format(value)
}