package com.uanish.expensetracker.data.model

enum class TxType { INCOME, EXPENSE }

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: TxType = TxType.EXPENSE,
    val categoryId: String = "",
    val note: String = "",
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
