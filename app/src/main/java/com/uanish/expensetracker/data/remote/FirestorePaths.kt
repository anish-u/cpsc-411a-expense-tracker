package com.uanish.expensetracker.data.remote

object FirestorePaths {
    fun userDoc(uid: String) = "users/$uid"
    fun categories(uid: String) = "users/$uid/categories"
    fun transactions(uid: String) = "users/$uid/transactions"
}
