package com.uanish.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uanish.expensetracker.viewmodel.CategoryExpense
import kotlin.math.max

@Composable
fun ExpensesByCategoryChart(
    items: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Expenses by Category", style = MaterialTheme.typography.titleMedium)

            if (items.isEmpty()) {
                Text(
                    "No expense data yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            val maxAmount = max(1.0, items.maxOf { it.amount })

            items.take(6).forEach { item ->
                val pct = (item.amount / maxAmount).coerceIn(0.0, 1.0).toFloat()
                val barColor = colorFromHex(item.colorHex)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // color dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(barColor)
                    )

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.categoryName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "$${"%.2f".format(item.amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // bar background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            // bar fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct)
                                    .height(8.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(barColor)
                            )
                        }
                    }
                }
            }

            if (items.size > 6) {
                Text(
                    "Showing top 6 categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
