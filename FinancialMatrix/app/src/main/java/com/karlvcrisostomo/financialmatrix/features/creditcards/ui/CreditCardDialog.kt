package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

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
import com.karlvcrisostomo.financialmatrix.features.creditcards.data.CreditCardEntity

@Composable
fun CreditCardDialog(
    card: CreditCardEntity? = null,
    onDismiss: () -> Unit,
    onSave: (CreditCardEntity) -> Unit
) {
    var name by remember { mutableStateOf(card?.name ?: "") }
    var billingDayText by remember { mutableStateOf(card?.billingDay?.toString() ?: "") }
    var dueDayText by remember { mutableStateOf(card?.dueDay?.toString() ?: "") }
    var limitText by remember { mutableStateOf(card?.creditLimit?.toString() ?: "") }

    val billingDay = billingDayText.toIntOrNull() ?: 0
    val dueDay = dueDayText.toIntOrNull() ?: 0
    val limit = limitText.toDoubleOrNull() ?: 0.0

    val isInputValid = name.isNotBlank() && 
            billingDay in 1..31 && 
            dueDay in 1..31 && 
            limit > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (card == null) "Add New Credit Card" else "Edit Credit Card") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Card Name (e.g., Visa Gold)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = billingDayText,
                    onValueChange = { billingDayText = it },
                    label = { Text("Billing Day (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDayText,
                    onValueChange = { dueDayText = it },
                    label = { Text("Due Day (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Credit Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        CreditCardEntity(
                            id = card?.id ?: 0,
                            name = name,
                            billingDay = billingDay,
                            dueDay = dueDay,
                            creditLimit = limit
                        )
                    )
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
