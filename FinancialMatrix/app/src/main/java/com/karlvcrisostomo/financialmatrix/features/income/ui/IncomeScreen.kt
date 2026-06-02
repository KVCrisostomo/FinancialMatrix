package com.karlvcrisostomo.financialmatrix.features.income.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.core.util.formatToHumanReadable
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeRepository
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionUiState
import com.karlvcrisostomo.financialmatrix.features.transactions.ui.TransactionViewModel
import java.util.Locale

@Composable
fun IncomeScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = uiState.userPreferences.currencySymbol
    val totalIncome = uiState.totalIncome

    Column(modifier = modifier.fillMaxSize()) {
        // Income Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Total Monthly Income", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", totalIncome)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        // We need a list of all income. Currently ViewModel only has total income.
        // Let's assume we want to show all income entries eventually.
        // For now, I'll just show the total or implement a basic list if I can get the data.
        // Actually, TransactionViewModel has incomeRepository.getAllIncome() but doesn't expose the list in UI state yet.
        // I should update TransactionUiState to include incomeList.
        
        Text(
            text = "Income History",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        if (uiState.incomeTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No income records found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                items(uiState.incomeTransactions) { income ->
                    IncomeItem(
                        income = income,
                        currencySymbol = currencySymbol,
                        onDelete = { viewModel.deleteIncome(income) }
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeItem(
    income: IncomeEntity,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = income.description, style = MaterialTheme.typography.titleMedium)
                Text(text = income.date.formatToHumanReadable(), style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", income.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2E7D32),
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
