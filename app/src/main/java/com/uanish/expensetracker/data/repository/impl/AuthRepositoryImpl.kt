package com.uanish.expensetracker.data.repository.impl

import com.uanish.expensetracker.data.repository.AuthRepository
import com.uanish.expensetracker.data.repository.UserSession
import com.uanish.expensetracker.util.AppResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val session: Flow<UserSession?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val u = fa.currentUser
            trySend(u?.let { UserSession(it.uid, it.email) })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(email: String, password: String): AppResult<Unit> = try {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Sign up failed.")
    }

    override suspend fun signIn(email: String, password: String): AppResult<Unit> = try {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(e.message ?: "Login failed.")
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
