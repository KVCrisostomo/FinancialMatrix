package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import java.time.LocalDate
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // 1. KOTLIN COLLECTION MATH: Calculate dashboard metrics on the fly
    val totalSpent = uiState.transactions.sumOf { it.amount }

    val cashSpent = uiState.transactions
        .filter { !it.isCreditCard }
        .sumOf { it.amount }

    val creditSpent = uiState.transactions
        .filter { it.isCreditCard }
        .sumOf { it.amount }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Financial Matrix Ledger") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 2. DASHBOARD CARD VIEW
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Outflow",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "₱${String.format("%.2f", totalSpent)}",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cash: ₱${String.format("%.2f", cashSpent)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Credit: ₱${String.format("%.2f", creditSpent)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 3. LEDGER LIST VIEW
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = "Error: ${uiState.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                uiState.transactions.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions found.\nTap the + button to seed data.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp, end = 16.dp, bottom = 16.dp
                        )
                    ) {
                        items(uiState.transactions, key = { it.id }) { transaction ->
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

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newTransaction ->
                viewModel.addTransaction(newTransaction)
                showAddDialog = false
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
                    text = "₱${String.format("%.2f", transaction.amount)}",
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