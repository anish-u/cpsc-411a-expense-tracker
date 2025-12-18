package com.uanish.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uanish.expensetracker.data.model.Category
import com.uanish.expensetracker.data.repository.FinanceRepository
import com.uanish.expensetracker.util.AppResult
import com.uanish.expensetracker.util.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val uid: String,
    private val repo: FinanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState<List<Category>>(isLoading = true))
    val state: StateFlow<UiState<List<Category>>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeCategories(uid)
                .onStart { _state.value = UiState(isLoading = true) }
                .catch { e -> _state.value = UiState(error = e.message ?: "Failed to load categories") }
                .collect { list -> _state.value = UiState(data = list) }
        }
    }

    fun upsert(name: String, colorHex: String, existingId: String? = null) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            _state.value = _state.value.copy(error = "Category name cannot be empty.")
            return
        }
        viewModelScope.launch {
            val category = Category(
                id = existingId.orEmpty(),
                name = trimmed,
                colorHex = colorHex
            )
            when (val res = repo.upsertCategory(uid, category)) {
                is AppResult.Success -> Unit
                is AppResult.Error -> _state.value = _state.value.copy(error = res.message)
            }
        }
    }

    fun delete(categoryId: String) {
        viewModelScope.launch {
            when (val res = repo.deleteCategory(uid, categoryId)) {
                is AppResult.Success -> Unit
                is AppResult.Error -> _state.value = _state.value.copy(error = res.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
