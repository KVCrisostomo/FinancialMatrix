package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.core.util.formatToHumanReadable
import com.karlvcrisostomo.financialmatrix.features.creditcards.ui.CreditCardViewModel
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringFrequency
import com.karlvcrisostomo.financialmatrix.features.transactions.data.RecurringTransactionEntity
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringManagementDialog(
    viewModel: TransactionViewModel,
    ccViewModel: CreditCardViewModel,
    onDismiss: () -> Unit
) {
    val recurringList by viewModel.recurringTransactions.collectAsState()
    val ccUiState by ccViewModel.uiState.collectAsState()
    var showAddRule by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recurring Transactions") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                if (recurringList.isEmpty()) {
                    Text("No recurring rules set.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recurringList) { rule ->
                            RecurringRuleItem(
                                rule = rule,
                                onDelete = { viewModel.deleteRecurringTransaction(rule) }
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { showAddRule = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Add New Rule")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (showAddRule) {
        AddRecurringRuleDialog(
            availableCards = ccUiState.cards.map { it.card.name },
            onDismiss = { showAddRule = false },
            onSave = {
                viewModel.addRecurringTransaction(it)
                showAddRule = false
            }
        )
    }
}

@Composable
fun RecurringRuleItem(
    rule: RecurringTransactionEntity,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.description, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "${rule.frequency.name} • ₱${String.format(Locale.US, "%.2f", rule.amount)}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Next: ${rule.nextOccurrence.formatToHumanReadable()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringRuleDialog(
    availableCards: List<String>,
    onDismiss: () -> Unit,
    onSave: (RecurringTransactionEntity) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
    var category by remember { mutableStateOf("Utilities") }
    var startDateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var isCreditCard by remember { mutableStateOf(false) }
    var selectedCardName by remember { mutableStateOf(if (availableCards.isNotEmpty()) availableCards[0] else "Primary") }

    val categories = listOf("Food", "Utilities", "Transport", "Entertainment", "Other")
    var catExpanded by remember { mutableStateOf(false) }
    var freqExpanded by remember { mutableStateOf(false) }
    var cardExpanded by remember { mutableStateOf(false) }

    val parsedStartDate = try { LocalDate.parse(startDateText) } catch (e: Exception) { null }
    val isInputValid = description.isNotBlank() && 
            amountText.toDoubleOrNull() != null && 
            parsedStartDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = freqExpanded,
                    onExpandedChange = { freqExpanded = !freqExpanded }
                ) {
                    OutlinedTextField(
                        value = frequency.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = freqExpanded,
                        onDismissRequest = { freqExpanded = false }
                    ) {
                        RecurringFrequency.values().forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f.name) },
                                onClick = {
                                    frequency = f
                                    freqExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCreditCard,
                        onCheckedChange = { isCreditCard = it }
                    )
                    Text(text = "Paid with Credit Card", modifier = Modifier.padding(start = 8.dp))
                }

                if (isCreditCard && availableCards.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = cardExpanded,
                        onExpandedChange = { cardExpanded = !cardExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCardName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Card") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = cardExpanded,
                            onDismissRequest = { cardExpanded = false }
                        ) {
                            availableCards.forEach { cardName ->
                                DropdownMenuItem(
                                    text = { Text(cardName) },
                                    onClick = {
                                        selectedCardName = cardName
                                        cardExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onSave(
                        RecurringTransactionEntity(
                            description = description,
                            amount = amount,
                            category = category,
                            isCreditCard = isCreditCard,
                            accountName = if (isCreditCard) selectedCardName else "Primary",
                            frequency = frequency,
                            startDate = parsedStartDate!!,
                            nextOccurrence = parsedStartDate
                        )
                    )
                },
                enabled = isInputValid
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
