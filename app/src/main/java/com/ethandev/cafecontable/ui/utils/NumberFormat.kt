package com.ethandev.cafecontable.ui.utils

fun formatNumber(value: String): String {
    return value
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}