package com.uanish.expensetracker.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uanish.expensetracker.ui.components.AppTopBar
import com.uanish.expensetracker.ui.components.ConfirmDeleteDialog
import com.uanish.expensetracker.viewmodel.TransactionDetailViewModel

@Composable
fun TransactionDetailScreen(
    vm: TransactionDetailViewModel,
    categoryName: (String) -> String, // pass mapping from categories
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = { AppTopBar("Transaction Detail", onBack) }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator()

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            state.data?.let { tx ->
                Text("Type: ${tx.type}")
                Text("Amount: $${"%.2f".format(tx.amount)}")
                Text("Category: ${categoryName(tx.categoryId)}")
                Text("Note: ${tx.note.ifBlank { "-" }}")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Button(onClick = onEdit, enabled = state.data != null) { Text("Edit") }
                Button(
                    onClick = { confirmDelete = true },
                    enabled = state.data != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Delete transaction?",
            text = "This action cannot be undone.",
            onConfirm = {
                confirmDelete = false
                vm.delete(onDone = onBack)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}
