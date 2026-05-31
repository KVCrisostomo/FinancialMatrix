package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import java.util.Locale // 1. Added explicit Java standard utility import

enum class TransactionSortOrder(val displayName: String) {
    LATEST(displayName = "Latest"),
    HIGHEST_AMOUNT(displayName = "Highest ₱"),
    LOWEST_AMOUNT(displayName = "Lowest ₱")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddDialog = remember { mutableStateOf(false) }
    var currentSortOrder by remember { mutableStateOf(TransactionSortOrder.LATEST) }

    val totalSpent = uiState.transactions.sumOf { it.amount }
    val cashSpent = uiState.transactions.filter { !it.isCreditCard }.sumOf { it.amount }
    val creditSpent = uiState.transactions.filter { it.isCreditCard }.sumOf { it.amount }

    val sortedTransactions = remember(uiState.transactions, currentSortOrder) {
        when (currentSortOrder) {
            TransactionSortOrder.LATEST -> uiState.transactions.sortedByDescending { it.id }
            TransactionSortOrder.HIGHEST_AMOUNT -> uiState.transactions.sortedByDescending { it.amount }
            TransactionSortOrder.LOWEST_AMOUNT -> uiState.transactions.sortedBy { it.amount }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Financial Matrix Ledger") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Dashboard Card View
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Outflow", style = MaterialTheme.typography.labelMedium)
                    Text(
                        // 2. Added explicit Locale protection
                        text = "₱${String.format(Locale.US, "%.2f", totalSpent)}",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Cash: ₱${String.format(Locale.US, "%.2f", cashSpent)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Credit: ₱${String.format(Locale.US, "%.2f", creditSpent)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Interactive Sorting Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TransactionSortOrder.entries.forEach { order ->
                    val isSelected = currentSortOrder == order
                    Box(
                        modifier = Modifier
                            .clickable { currentSortOrder = order }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = order.displayName,
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
                sortedTransactions.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No transactions found.\nTap the + button to seed data.", modifier = Modifier.padding(16.dp))
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        items(sortedTransactions, key = { it.id }) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onDelete = { viewModel.deleteTransaction(transaction) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog.value) {
        AddTransactionDialog(
            onDismiss = { showAddDialog.value = false },
            onSave = { newTransaction ->
                viewModel.addTransaction(newTransaction)
                showAddDialog.value = false
            }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.description, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${transaction.category} • ${if (transaction.isCreditCard) "Credit" else "Cash"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(text = transaction.date.toString(), style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    // 3. Added explicit Locale protection
                    text = "₱${String.format(Locale.US, "%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}