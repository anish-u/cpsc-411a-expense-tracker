package com.uanish.expensetracker.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uanish.expensetracker.ui.components.ExpensesByCategoryChart
import com.uanish.expensetracker.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    vm: DashboardViewModel,
    onGoTransactions: () -> Unit,
    onGoCategories: () -> Unit,
    onGoProfile: () -> Unit,
    onOpenTx: (String) -> Unit
) {
    val ui = vm.ui.collectAsStateWithLifecycle().value

    Scaffold { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Income",
                    value = "$${"%.2f".format(ui.income)}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Expenses",
                    value = "$${"%.2f".format(ui.expense)}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Balance",
                    value = "$${"%.2f".format(ui.balance)}",
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(onClick = onGoProfile, modifier = Modifier.fillMaxWidth()) { Text("Profile") }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGoTransactions, modifier = Modifier.weight(1f)) { Text("Transactions") }
                Button(onClick = onGoCategories, modifier = Modifier.weight(1f)) { Text("Categories") }
            }

            ExpensesByCategoryChart(items = ui.expenseByCategory)


            Text("Recent", style = MaterialTheme.typography.titleMedium)

            if (ui.recent.isEmpty()) {
                Text("No transactions yet. Add one from Transactions.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ui.recent, key = { it.id }) { tx ->
                        Card(onClick = { onOpenTx(tx.id) }) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text("${tx.type}  $${"%.2f".format(tx.amount)}", style = MaterialTheme.typography.titleMedium)
                                if (tx.note.isNotBlank()) Text(tx.note)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}