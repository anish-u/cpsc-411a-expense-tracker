package com.uanish.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uanish.expensetracker.data.model.Transaction
import com.uanish.expensetracker.data.repository.FinanceRepository
import com.uanish.expensetracker.util.AppResult
import com.uanish.expensetracker.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val uid: String,
    private val txId: String,
    private val repo: FinanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState<Transaction>(isLoading = true))
    val state: StateFlow<UiState<Transaction>> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)
            when (val res = repo.getTransaction(uid, txId)) {
                is AppResult.Success -> _state.value = UiState(data = res.data)
                is AppResult.Error -> _state.value = UiState(error = res.message)
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = repo.deleteTransaction(uid, txId)) {
                is AppResult.Success -> onDone()
                is AppResult.Error -> _state.value = _state.value.copy(isLoading = false, error = res.message)
            }
        }
    }
}
