package com.uanish.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uanish.expensetracker.data.model.TxType
import com.uanish.expensetracker.data.model.Transaction
import com.uanish.expensetracker.data.repository.FinanceRepository
import com.uanish.expensetracker.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TxEditUi(
    val isLoading: Boolean = false,
    val error: String? = null,

    val amountText: String = "",
    val type: TxType = TxType.EXPENSE,
    val categoryId: String? = null,
    val note: String = "",

    // keep originals when editing (so we don't overwrite them)
    val originalCreatedAt: Long? = null,
    val originalDateEpochMillis: Long? = null,

    // NEW: date the user selected (or loaded when editing)
    val selectedDateEpochMillis: Long? = null
)

class TransactionEditViewModel(
    private val uid: String,
    private val txId: String,
    private val repo: FinanceRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(TxEditUi(isLoading = txId.isNotBlank()))
    val ui: StateFlow<TxEditUi> = _ui.asStateFlow()

    fun loadIfEditing() {
        if (txId.isBlank()) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            when (val res = repo.getTransaction(uid, txId)) {
                is AppResult.Success -> {
                    val t = res.data
                    _ui.value = TxEditUi(
                        isLoading = false,
                        amountText = t.amount.toString(),
                        type = t.type,
                        categoryId = t.categoryId.ifBlank { null },
                        note = t.note,
                        originalCreatedAt = t.createdAt,
                        originalDateEpochMillis = t.dateEpochMillis,
                        selectedDateEpochMillis = t.dateEpochMillis // ✅ load existing date into picker
                    )
                }
                is AppResult.Error -> _ui.value = _ui.value.copy(isLoading = false, error = res.message)
            }
        }
    }

    fun setAmount(v: String) { _ui.value = _ui.value.copy(amountText = v) }
    fun setType(t: TxType) { _ui.value = _ui.value.copy(type = t) }
    fun setCategory(id: String) { _ui.value = _ui.value.copy(categoryId = id) }
    fun setNote(v: String) { _ui.value = _ui.value.copy(note = v) }
    fun setDate(epochMillis: Long) { _ui.value = _ui.value.copy(selectedDateEpochMillis = epochMillis) }

    fun clearError() { _ui.value = _ui.value.copy(error = null) }

    fun save(onDone: () -> Unit) {
        val amount = _ui.value.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _ui.value = _ui.value.copy(error = "Enter a valid amount > 0")
            return
        }

        val catId = _ui.value.categoryId
        if (catId.isNullOrBlank()) {
            _ui.value = _ui.value.copy(error = "Please select a category.")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)

            val now = System.currentTimeMillis()
            val createdAt = _ui.value.originalCreatedAt ?: now

            val date = _ui.value.selectedDateEpochMillis ?: (_ui.value.originalDateEpochMillis ?: now)

            val tx = Transaction(
                id = txId,
                amount = amount,
                type = _ui.value.type,
                categoryId = catId,
                note = _ui.value.note.trim(),
                dateEpochMillis = date,
                createdAt = createdAt
            )

            when (val res = repo.upsertTransaction(uid, tx)) {
                is AppResult.Success -> onDone()
                is AppResult.Error -> _ui.value = _ui.value.copy(isLoading = false, error = res.message)
            }
        }
    }
}
