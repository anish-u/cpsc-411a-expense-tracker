package com.uanish.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uanish.expensetracker.data.model.Category
import com.uanish.expensetracker.data.model.TxType
import com.uanish.expensetracker.data.model.Transaction
import com.uanish.expensetracker.data.repository.FinanceRepository
import com.uanish.expensetracker.data.repository.TxFilter
import kotlinx.coroutines.flow.*
import kotlin.math.roundToLong

data class CategoryExpense(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String,
    val amount: Double
)

data class DashboardUi(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
    val recent: List<Transaction> = emptyList(),
    val expenseByCategory: List<CategoryExpense> = emptyList()
)

class DashboardViewModel(
    private val uid: String,
    private val repo: FinanceRepository
) : ViewModel() {

    private val recentTxFlow: Flow<List<Transaction>> =
        repo.observeTransactions(uid, TxFilter(limit = 20, newestFirst = true))

    private val categoriesFlow: Flow<List<Category>> =
        repo.observeCategories(uid)

    val ui: StateFlow<DashboardUi> =
        combine(recentTxFlow, categoriesFlow) { txs, cats ->
            val income = txs.filter { it.type == TxType.INCOME }.sumOf { it.amount }
            val expense = txs.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
            val balance = income - expense

            val catById = cats.associateBy { it.id }

            // Group EXPENSE transactions by categoryId
            val expenseByCategory = txs
                .asSequence()
                .filter { it.type == TxType.EXPENSE }
                .groupBy { it.categoryId }
                .map { (catId, list) ->
                    val c = catById[catId]
                    CategoryExpense(
                        categoryId = catId,
                        categoryName = c?.name ?: "Unknown",
                        colorHex = c?.colorHex ?: "#6750A4",
                        amount = list.sumOf { it.amount }
                    )
                }
                .sortedByDescending { it.amount }

            DashboardUi(
                income = income,
                expense = expense,
                balance = balance,
                recent = txs.take(10),
                expenseByCategory = expenseByCategory
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUi())
}
