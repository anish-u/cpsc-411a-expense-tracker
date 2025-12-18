package com.uanish.expensetracker.ui.components

import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String, fallback: Color = Color(0xFF6750A4)): Color {
    val cleaned = hex.trim().removePrefix("#")
    return try {
        val value = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color((0xFF000000 or value).toInt()) // RRGGBB
            8 -> Color(value.toInt())                 // AARRGGBB
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
