package com.uanish.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uanish.expensetracker.data.model.Category
import com.uanish.expensetracker.data.model.Transaction
import com.uanish.expensetracker.data.model.TxType
import com.uanish.expensetracker.data.repository.FinanceRepository
import com.uanish.expensetracker.data.repository.TxFilter
import com.uanish.expensetracker.util.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TxUi(
    val search: String = "",
    val type: TxType? = null,
    val categoryId: String? = null,
    val newestFirst: Boolean = true,
    val startEpochMillis: Long? = null,
    val endEpochMillis: Long? = null
)

class TransactionsViewModel(
    private val uid: String,
    private val repo: FinanceRepository
) : ViewModel() {

    private val _filters = MutableStateFlow(TxUi())
    val filters: StateFlow<TxUi> = _filters.asStateFlow()

    val categories: StateFlow<List<Category>> =
        repo.observeCategories(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state: StateFlow<UiState<List<Transaction>>> =
        _filters
            .flatMapLatest { f ->
                repo.observeTransactions(
                    uid = uid,
                    filter = TxFilter(
                        type = f.type,
                        categoryId = f.categoryId,
                        searchNote = f.search,
                        newestFirst = f.newestFirst,
                        startEpochMillis = f.startEpochMillis,
                        endEpochMillis = f.endEpochMillis
                    )
                )
            }
            .map<List<Transaction>, UiState<List<Transaction>>> { UiState(data = it) }
            .onStart { emit(UiState(isLoading = true)) }
            .catch { e -> emit(UiState(error = e.message ?: "Failed to load transactions")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState(isLoading = true))

    fun setSearch(s: String) { _filters.value = _filters.value.copy(search = s) }
    fun setType(t: TxType?) { _filters.value = _filters.value.copy(type = t) }
    fun setCategory(id: String?) { _filters.value = _filters.value.copy(categoryId = id) }
    fun toggleSort() { _filters.value = _filters.value.copy(newestFirst = !_filters.value.newestFirst) }

    fun clearDates() { _filters.value = _filters.value.copy(startEpochMillis = null, endEpochMillis = null) }

    fun setStartDate(ms: Long?) {
        val end = _filters.value.endEpochMillis
        _filters.value = _filters.value.copy(
            startEpochMillis = ms,
            endEpochMillis = if (ms != null && end != null && end < ms) null else end
        )
    }

    fun setEndDate(ms: Long?) {
        val start = _filters.value.startEpochMillis
        _filters.value = _filters.value.copy(
            endEpochMillis = ms,
            startEpochMillis = if (ms != null && start != null && start > ms) null else start
        )
    }

}
