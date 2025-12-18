package com.uanish.expensetracker.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val colorHex: String = "#6750A4",
    val createdAt: Long = System.currentTimeMillis()
)