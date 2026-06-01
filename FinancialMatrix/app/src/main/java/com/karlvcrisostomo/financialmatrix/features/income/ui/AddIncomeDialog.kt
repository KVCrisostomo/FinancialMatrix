package com.karlvcrisostomo.financialmatrix.features.income.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.features.income.data.IncomeEntity
import java.time.LocalDate

@Composable
fun AddIncomeDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (IncomeEntity) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    val isAmountValid = remember(amountText) {
        amountText.toDoubleOrNull()?.let { it > 0.0 } ?: false
    }
    val isDescriptionValid = remember(description) {
        description.isNotBlank()
    }
    val isInputValid = isDescriptionValid && isAmountValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Income") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
                    val newIncome = IncomeEntity(
                        description = description,
                        amount = finalAmount,
                        date = LocalDate.now()
                    )
                    onSave(newIncome)
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
