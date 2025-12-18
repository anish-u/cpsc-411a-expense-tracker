package com.uanish.expensetracker.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uanish.expensetracker.data.model.Category
import com.uanish.expensetracker.data.model.TxType
import com.uanish.expensetracker.ui.components.AppTopBar
import com.uanish.expensetracker.viewmodel.TransactionEditViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(
    vm: TransactionEditViewModel,
    categories: List<Category>,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    var typeExpanded by remember { mutableStateOf(false) }
    var catExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadIfEditing() }

    val selectedCatName = categories.firstOrNull { it.id == ui.categoryId }?.name ?: "Select category"

    val dateText = remember(ui.selectedDateEpochMillis) {
        val ms = ui.selectedDateEpochMillis
        if (ms == null) "Select date"
        else SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ms))
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (ui.originalCreatedAt == null && ui.originalDateEpochMillis == null && ui.amountText.isBlank())
                    "Add Transaction" else "Edit Transaction",
                onBack = onBack
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.isLoading) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Text("Loading...")
                }
            }

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = ui.amountText,
                onValueChange = vm::setAmount,
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.isLoading
            )

            // Type dropdown
            Box {
                OutlinedButton(
                    onClick = { typeExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ui.isLoading
                ) { Text("Type: ${ui.type.name}") }

                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("EXPENSE") },
                        onClick = { vm.setType(TxType.EXPENSE); typeExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("INCOME") },
                        onClick = { vm.setType(TxType.INCOME); typeExpanded = false }
                    )
                }
            }

            Box {
                OutlinedButton(
                    onClick = { catExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !ui.isLoading
                ) { Text("Category: $selectedCatName") }

                DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = { vm.setCategory(c.id); catExpanded = false }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.isLoading
            ) {
                Text("Date: $dateText")
            }

            OutlinedTextField(
                value = ui.note,
                onValueChange = vm::setNote,
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.isLoading
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, enabled = !ui.isLoading) { Text("Cancel") }
                Button(
                    onClick = { vm.save(onDone) },
                    enabled = !ui.isLoading,
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = ui.selectedDateEpochMillis ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = pickerState.selectedDateMillis
                    if (selected != null) vm.setDate(selected)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
