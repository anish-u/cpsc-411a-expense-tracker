package com.uanish.expensetracker.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uanish.expensetracker.ui.components.AppTopBar

@Composable
fun ProfileScreen(
    email: String?,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar("Profile", onBack) }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Email: ${email ?: "Unknown"}")

            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out")
            }
        }
    }
}
