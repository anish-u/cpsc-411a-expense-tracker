package com.uanish.expensetracker.data.repository

import com.uanish.expensetracker.data.model.Category
import com.uanish.expensetracker.data.model.Transaction
import com.uanish.expensetracker.data.model.TxType
import com.uanish.expensetracker.util.AppResult
import kotlinx.coroutines.flow.Flow

data class TxFilter(
    val type: TxType? = null,
    val categoryId: String? = null,
    val searchNote: String? = null,
    val limit: Long? = null,
    val newestFirst: Boolean = true,
    val startEpochMillis: Long? = null,
    val endEpochMillis: Long? = null
)


interface FinanceRepository {
    fun observeCategories(uid: String): Flow<List<Category>>
    fun observeTransactions(uid: String, filter: TxFilter): Flow<List<Transaction>>

    suspend fun upsertCategory(uid: String, category: Category): AppResult<Unit>
    suspend fun deleteCategory(uid: String, categoryId: String): AppResult<Unit>

    suspend fun upsertTransaction(uid: String, tx: Transaction): AppResult<Unit>
    suspend fun deleteTransaction(uid: String, txId: String): AppResult<Unit>
    suspend fun getTransaction(uid: String, txId: String): AppResult<Transaction>
    suspend fun getCategory(uid: String, categoryId: String): AppResult<Category>

}
