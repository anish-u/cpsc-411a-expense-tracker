package com.uanish.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uanish.expensetracker.data.repository.AuthRepository
import com.uanish.expensetracker.util.AppResult
import com.uanish.expensetracker.util.Validators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val uid: String, val email: String?) : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.session.collect { session ->
                _authState.value =
                    if (session == null) AuthState.Unauthenticated
                    else AuthState.Authenticated(session.uid, session.email)
            }
        }
    }

    fun signUp(email: String, password: String) {
        val e = email.trim()
        if (!Validators.isValidEmail(e)) {
            _authState.value = AuthState.Error("Please enter a valid email.")
            return
        }
        Validators.passwordError(password)?.let {
            _authState.value = AuthState.Error(it)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val res = repo.signUp(e, password)) {
                is AppResult.Success -> Unit // session flow will update state
                is AppResult.Error -> _authState.value = AuthState.Error(res.message)
            }
        }
    }

    fun signIn(email: String, password: String) {
        val e = email.trim()
        if (!Validators.isValidEmail(e)) {
            _authState.value = AuthState.Error("Please enter a valid email.")
            return
        }
        Validators.passwordError(password)?.let {
            _authState.value = AuthState.Error(it)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val res = repo.signIn(e, password)) {
                is AppResult.Success -> Unit
                is AppResult.Error -> _authState.value = AuthState.Error(res.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { repo.signOut() }
    }

    fun clearError() {
        val current = _authState.value
        if (current is AuthState.Error) _authState.value = AuthState.Unauthenticated
    }
}
