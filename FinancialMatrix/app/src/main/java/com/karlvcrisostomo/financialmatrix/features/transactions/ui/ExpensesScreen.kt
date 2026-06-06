package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.core.util.formatToHumanReadable
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.ui.theme.AlertRed
import com.karlvcrisostomo.financialmatrix.ui.theme.SuccessGreen
import com.karlvcrisostomo.financialmatrix.ui.theme.WarningOrange
import java.util.Locale

@Composable
fun ExpensesScreen(
    uiState: TransactionUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onSortOrderChange: (TransactionSortOrder) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencySymbol = uiState.userPreferences.currencySymbol
    val totalSpent = uiState.totalSpent
    val cashSpent = uiState.cashSpent
    val creditSpent = uiState.creditSpent
    val categoryAmounts = uiState.categoryAmounts
    
    val cashPercentage = if (totalSpent > 0) (cashSpent / totalSpent * 100) else 0.0
    val creditPercentage = if (totalSpent > 0) (creditSpent / totalSpent * 100) else 0.0

    val budgetLimit = uiState.userPreferences.monthlyBudgetLimit
    val remainingBudget = budgetLimit - totalSpent
    val budgetPercentage = if (budgetLimit > 0) (totalSpent / budgetLimit) else 0.0

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Dashboard Card View
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Monthly Outflow", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", totalSpent)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Cash: $currencySymbol${String.format(Locale.US, "%.2f", cashSpent)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${String.format(Locale.US, "%.1f", cashPercentage)}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Credit: $currencySymbol${String.format(Locale.US, "%.2f", creditSpent)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${String.format(Locale.US, "%.1f", creditPercentage)}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
                
                if (totalSpent > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Spending Distribution", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    SpendingDistributionChart(
                        categoryAmounts = categoryAmounts,
                        totalAmount = totalSpent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                    )
                }
                
                if (budgetLimit > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(text = "Monthly Budget", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", budgetLimit)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (remainingBudget >= 0) "Remaining" else "Over Budget",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (remainingBudget >= 0) Color.Unspecified else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", Math.abs(remainingBudget))}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBudget >= 0) Color.Unspecified else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    LinearProgressIndicator(
                        progress = { budgetPercentage.coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(8.dp),
                        color = when {
                            budgetPercentage >= 1.0 -> MaterialTheme.colorScheme.error
                            budgetPercentage >= 0.8 -> WarningOrange // Orange
                            else -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        // Category Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { onCategoryFilterChange(null) },
                label = { Text("All") }
            )
            uiState.availableCategories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { onCategoryFilterChange(category) },
                    label = { Text(category) }
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search description or category...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Interactive Sorting Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransactionSortOrder.entries.forEach { order ->
                val isSelected = uiState.sortOrder == order
                Box(
                    modifier = Modifier
                        .clickable { onSortOrderChange(order) }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = order.displayName.replace("₱", currencySymbol),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }

        // Ledger List View
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Text(text = "Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            uiState.transactions.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No transactions found.\nTap the + button to seed data.", modifier = Modifier.padding(16.dp))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    items(uiState.transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            currencySymbol = currencySymbol,
                            onDelete = { onDeleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    currencySymbol: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    CategoryBadge(category = transaction.category)
                    Text(
                        text = if (transaction.isCreditCard) "Credit" else "Cash",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.date.formatToHumanReadable(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", transaction.amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).padding(start = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    val containerColor = when (category) {
        "Food" -> MaterialTheme.colorScheme.errorContainer
        "Utilities" -> MaterialTheme.colorScheme.tertiaryContainer
        "Transport" -> MaterialTheme.colorScheme.primaryContainer
        "Entertainment" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (category) {
        "Food" -> MaterialTheme.colorScheme.onErrorContainer
        "Utilities" -> MaterialTheme.colorScheme.onTertiaryContainer
        "Transport" -> MaterialTheme.colorScheme.onPrimaryContainer
        "Entertainment" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SpendingDistributionChart(
    categoryAmounts: Map<String, Double>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val categoryColors = mapOf(
        "Food" to MaterialTheme.colorScheme.error,
        "Utilities" to MaterialTheme.colorScheme.tertiary,
        "Transport" to MaterialTheme.colorScheme.primary,
        "Entertainment" to MaterialTheme.colorScheme.secondary,
        "CC Payment" to MaterialTheme.colorScheme.outline,
        "Other" to MaterialTheme.colorScheme.outline
    )

    Canvas(modifier = modifier) {
        var currentX = 0f
        val canvasWidth = size.width
        val canvasHeight = size.height

        categoryAmounts.forEach { (category, amount) ->
            val fraction = (amount / totalAmount).toFloat()
            val segmentWidth = canvasWidth * fraction
            val color = categoryColors[category] ?: Color.Gray

            drawRect(
                color = color,
                topLeft = Offset(currentX, 0f),
                size = Size(segmentWidth, canvasHeight)
            )
            currentX += segmentWidth
        }
    }
}
