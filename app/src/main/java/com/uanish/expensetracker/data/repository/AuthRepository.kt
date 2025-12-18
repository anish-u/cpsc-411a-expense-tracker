package com.uanish.expensetracker.data.repository

import com.uanish.expensetracker.util.AppResult
import kotlinx.coroutines.flow.Flow

data class UserSession(val uid: String, val email: String?)

interface AuthRepository {
    val session: Flow<UserSession?>
    suspend fun signUp(email: String, password: String): AppResult<Unit>
    suspend fun signIn(email: String, password: String): AppResult<Unit>
    suspend fun signOut()
}
