package com.uanish.expensetracker.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uanish.expensetracker.data.model.Category
import com.uanish.expensetracker.ui.components.AppAddFab
import com.uanish.expensetracker.ui.components.AppTopBar
import com.uanish.expensetracker.ui.components.ConfirmDeleteDialog
import com.uanish.expensetracker.viewmodel.CategoriesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke


@Composable
fun CategoriesScreen(vm: CategoriesViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()

    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = { AppTopBar("Categories", onBack = onBack) },
        floatingActionButton = {
            AppAddFab(
                onClick = {
                    editing = null
                    showEditor = true
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Categories", style = MaterialTheme.typography.headlineMedium)

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.error?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = vm::clearError) { Text("Dismiss") }
            }

            val list = state.data.orEmpty()
            if (!state.isLoading && list.isEmpty()) {
                Text("No categories yet. Tap + to add one.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cat.name, style = MaterialTheme.typography.titleMedium)
                                Text(cat.colorHex, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { editing = cat; showEditor = true }) { Text("Edit") }
                            TextButton(onClick = { deleteTarget = cat }) { Text("Delete") }
                        }
                    }

                }
            }
        }
    }

    if (showEditor) {
        CategoryEditorDialog(
            initial = editing,
            onSave = { name, color ->
                vm.upsert(name, color, existingId = editing?.id)
                showEditor = false
            },
            onDismiss = { showEditor = false }
        )
    }

    deleteTarget?.let { cat ->
        ConfirmDeleteDialog(
            title = "Delete category?",
            text = "This will delete '${cat.name}'.",
            onConfirm = {
                vm.delete(cat.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    initial: com.uanish.expensetracker.data.model.Category?,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var colorHex by remember { mutableStateOf(initial?.colorHex ?: "#6750A4") }

    val palette = listOf(
        "#6750A4", "#1D4ED8", "#0F766E", "#16A34A",
        "#EAB308", "#F97316", "#DC2626", "#DB2777",
        "#7C3AED", "#111827"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Category" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pick a color", style = MaterialTheme.typography.labelLarge)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    palette.forEach { hex ->
                        val isSelected = hex.equals(colorHex, ignoreCase = true)
                        val dot = com.uanish.expensetracker.ui.components.colorFromHex(hex)

                        Surface(
                            shape = CircleShape,
                            tonalElevation = if (isSelected) 4.dp else 0.dp,
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.size(34.dp),
                            onClick = { colorHex = hex }
                        ) {
                            Box(Modifier.fillMaxSize().background(dot))
                        }
                    }
                }

                // Optional: manual hex for power users
                OutlinedTextField(
                    value = colorHex,
                    onValueChange = { colorHex = it },
                    label = { Text("Color (Hex)") },
                    supportingText = { Text("Example: #6750A4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, colorHex) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
