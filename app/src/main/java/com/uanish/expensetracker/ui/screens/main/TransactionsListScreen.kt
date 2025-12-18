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
import com.uanish.expensetracker.data.model.TxType
import com.uanish.expensetracker.ui.components.AppTopBar
import com.uanish.expensetracker.ui.components.TransactionRow
import com.uanish.expensetracker.viewmodel.TransactionsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    vm: TransactionsViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()

    val catNameById = remember(categories) { categories.associate { it.id to it.name } }
    val catColorById = remember(categories) { categories.associate { it.id to it.colorHex } }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    fun formatDate(ms: Long) = dateFmt.format(Date(ms))


    Scaffold(
        topBar = { AppTopBar("Transactions", onBack) },
        floatingActionButton = { com.uanish.expensetracker.ui.components.AppAddFab(onAdd) }

    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = filters.search,
                onValueChange = vm::setSearch,
                label = { Text("Search note...") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TypeDropdown(selected = filters.type, onSelected = vm::setType, modifier = Modifier.weight(1f))
                CategoryDropdown(categories = categories, selectedId = filters.categoryId, onSelected = vm::setCategory, modifier = Modifier.weight(1f))
            }

            // Date range controls
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (filters.startEpochMillis == null) "Start date"
                        else "Start: ${formatDate(filters.startEpochMillis!!)}"
                    )
                }
                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (filters.endEpochMillis == null) "End date"
                        else "End: ${formatDate(filters.endEpochMillis!!)}"
                    )
                }
            }
            TextButton(onClick = vm::clearDates) { Text("Clear dates") }

            TextButton(onClick = vm::toggleSort) {
                Text(if (filters.newestFirst) "Sort: Newest" else "Sort: Oldest")
            }

            if (state.isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Loading...")
                }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            val list = state.data.orEmpty()
            if (!state.isLoading && list.isEmpty()) {
                Text("No transactions yet. Tap + to add one.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { tx ->
                    TransactionRow(
                        tx = tx,
                        categoryName = catNameById[tx.categoryId] ?: "Unknown",
                        categoryColorHex = catColorById[tx.categoryId] ?: "#6750A4",
                        onOpen = { onOpen(tx.id) },
                        onEdit = { onEdit(tx.id) }
                    )
                }
            }
        }
    }

    if (showStartPicker) {
        DatePickerDialogWrapper(
            title = "Select start date",
            onDismiss = { showStartPicker = false },
            onConfirm = { ms -> vm.setStartDate(ms) }
        )
    }
    if (showEndPicker) {
        DatePickerDialogWrapper(
            title = "Select end date",
            onDismiss = { showEndPicker = false },
            onConfirm = { ms -> vm.setEndDate(ms) }
        )
    }
}

@Composable
private fun TypeDropdown(
    selected: TxType?,
    onSelected: (TxType?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selected?.name ?: "All types"

    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All types") }, onClick = { onSelected(null); expanded = false })
            DropdownMenuItem(text = { Text("INCOME") }, onClick = { onSelected(TxType.INCOME); expanded = false })
            DropdownMenuItem(text = { Text("EXPENSE") }, onClick = { onSelected(TxType.EXPENSE); expanded = false })
        }
    }
}

@Composable
private fun CategoryDropdown(
    categories: List<com.uanish.expensetracker.data.model.Category>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedId }?.name ?: "All categories"

    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(selectedName) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All categories") }, onClick = { onSelected(null); expanded = false })
            categories.forEach { c ->
                DropdownMenuItem(text = { Text(c.name) }, onClick = { onSelected(c.id); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogWrapper(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val state = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val sel = state.selectedDateMillis
                if (sel != null) onConfirm(sel)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            DatePicker(state = state)
        }
    }
}
