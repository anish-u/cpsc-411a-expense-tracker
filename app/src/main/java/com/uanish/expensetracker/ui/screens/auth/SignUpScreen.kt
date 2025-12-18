package com.uanish.expensetracker.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uanish.expensetracker.util.Validators
import com.uanish.expensetracker.viewmodel.AuthState

@Composable
fun SignUpScreen(
    authState: AuthState,
    onSignup: (String, String) -> Unit,
    onGoLogin: () -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val emailError = remember(email) {
        if (email.isBlank()) null
        else if (!Validators.isValidEmail(email)) "Invalid email format." else null
    }

    val pwError = remember(password) {
        if (password.isBlank()) null else Validators.passwordError(password)
    }

    val confirmError = remember(password, confirmPassword) {
        if (confirmPassword.isBlank()) null
        else if (confirmPassword != password) "Passwords do not match." else null
    }

    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    val canSubmit =
        !isLoading &&
                emailError == null &&
                pwError == null &&
                confirmError == null &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank()

    Scaffold { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create Account", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                isError = emailError != null,
                supportingText = { if (emailError != null) Text(emailError) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                isError = pwError != null && password.isNotBlank(),
                supportingText = {
                    if (pwError != null && password.isNotBlank()) Text(pwError)
                    else Text("Minimum 6 characters")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                isError = confirmError != null && confirmPassword.isNotBlank(),
                supportingText = { if (confirmError != null && confirmPassword.isNotBlank()) Text(confirmError) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onClearError) { Text("Dismiss") }
            }

            Button(
                onClick = { onSignup(email, password) },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Sign Up")
                }
            }

            TextButton(onClick = onGoLogin) { Text("Back to login") }
        }
    }
}
