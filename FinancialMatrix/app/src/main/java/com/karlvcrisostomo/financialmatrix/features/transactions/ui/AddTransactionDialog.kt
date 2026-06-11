package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.domain.model.TransactionCategory
import com.karlvcrisostomo.financialmatrix.features.transactions.data.TransactionEntity
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialIsCreditCard: Boolean,
    currencySymbol: String,
    availableCards: List<CreditCardEntity>,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var isCreditCard by remember { mutableStateOf(initialIsCreditCard) }
    var selectedCardName by remember { mutableStateOf("") }
    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var targetCardId by remember { mutableStateOf<Long?>(null) }
    
    // Auto-select the first card when availableCards changes
    LaunchedEffect(availableCards) {
        if (availableCards.isNotEmpty()) {
            if (selectedCardId == null) {
                selectedCardId = availableCards[0].id
                selectedCardName = availableCards[0].name
            }
        } else {
            selectedCardId = null
            selectedCardName = "Primary"
        }
    }
    
    val categories = listOf("Food", "Utilities", "Transport", "Entertainment", "CC Payment", "Other")
    var categoryExpanded by remember { mutableStateOf(false) }
    var cardExpanded by remember { mutableStateOf(false) }
    var targetCardExpanded by remember { mutableStateOf(false) }

    val isAmountValid = remember(amountText) {
        amountText.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } ?: false
    }
    val isDescriptionValid = remember(description) {
        description.isNotBlank()
    }
    val isTargetCardValid = remember(category, targetCardId) {
        if (category == "CC Payment") targetCardId != null else true
    }
    val isSourceCardValid = remember(isCreditCard, selectedCardId) {
        if (isCreditCard) selectedCardId != null else true
    }
    val isInputValid = isDescriptionValid && isAmountValid && isTargetCardValid && isSourceCardValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountText.isNotEmpty() && !isAmountValid,
                    supportingText = {
                        if (amountText.isNotEmpty() && !isAmountValid) {
                            Text("Please enter a positive numeric amount")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    categoryExpanded = false
                                    if (category == "CC Payment") {
                                        isCreditCard = false
                                    } else {
                                        targetCardId = null
                                    }
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                if (category == "CC Payment") {
                    ExposedDropdownMenuBox(
                        expanded = targetCardExpanded,
                        onExpandedChange = { targetCardExpanded = !targetCardExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = availableCards.find { it.id == targetCardId }?.name ?: "Select Target Card",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Credit Card") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetCardExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = targetCardExpanded,
                            onDismissRequest = { targetCardExpanded = false }
                        ) {
                            availableCards.forEach { card ->
                                DropdownMenuItem(
                                    text = { Text(card.name) },
                                    onClick = {
                                        targetCardId = card.id
                                        targetCardExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }

                if (category != "CC Payment") {
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
                            label = { Text("Source Card") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = cardExpanded,
                            onDismissRequest = { cardExpanded = false }
                        ) {
                            availableCards.forEach { card ->
                                DropdownMenuItem(
                                    text = { Text(card.name) },
                                    onClick = {
                                        selectedCardId = card.id
                                        selectedCardName = card.name
                                        cardExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
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
                    val finalAmount = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val newTransaction = TransactionEntity(
                        description = description,
                        amount = finalAmount,
                        date = LocalDate.now(),
                        category = category,
                        isCreditCard = isCreditCard,
                        accountName = if (isCreditCard) selectedCardName else "Primary",
                        targetCreditCardId = if (category == "CC Payment") targetCardId else selectedCardId
                    )
                    onSave(newTransaction)
                },
                enabled = isInputValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}