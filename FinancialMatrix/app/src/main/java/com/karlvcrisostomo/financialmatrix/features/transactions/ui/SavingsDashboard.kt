package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.ui.theme.AlertRed
import com.karlvcrisostomo.financialmatrix.ui.theme.SuccessGreen
import com.karlvcrisostomo.financialmatrix.ui.theme.WarningOrange
import java.util.Locale

@Composable
fun SavingsDashboard(
    netSavings: Double,
    savingsRate: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val healthColor = when {
        savingsRate >= 30 -> SuccessGreen // Strong Green
        savingsRate >= 10 -> WarningOrange // Orange
        savingsRate > 0 -> AlertRed // Red
        else -> Color.Gray
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Savings",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", netSavings)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (netSavings >= 0) SuccessGreen else MaterialTheme.colorScheme.error
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Savings Rate",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", savingsRate)}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = healthColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (savingsRate / 100).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = healthColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Text(
                text = when {
                    savingsRate >= 30 -> "Excellent! You are building wealth rapidly."
                    savingsRate >= 10 -> "Good progress. Keep it up!"
                    savingsRate > 0 -> "Tight margins. Look for ways to save."
                    else -> "Warning: Spending exceeded income this month."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
