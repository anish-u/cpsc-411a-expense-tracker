package com.uanish.expensetracker.util

import android.util.Patterns

object Validators {
    fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    fun passwordError(pw: String): String? =
        if (pw.length < 6) "Password must be at least 6 characters." else null
}
