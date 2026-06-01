package com.karlvcrisostomo.financialmatrix.features.creditcards.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karlvcrisostomo.financialmatrix.core.util.formatToHumanReadable
import java.util.Locale

@Composable
fun CreditCardDashboard(
    uiState: CreditCardUiState,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (uiState.cards.isNotEmpty()) {
        Column(modifier = modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Credit Card Monitoring",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.cards) { stats ->
                    CreditCardSummaryCard(stats = stats, currencySymbol = currencySymbol)
                }
            }
        }
    }
}

@Composable
fun CreditCardSummaryCard(
    stats: CreditCardStats,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(280.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stats.card.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "Due: Day ${stats.card.dueDay}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Statement Balance", style = MaterialTheme.typography.labelSmall)
            Text(
                text = "$currencySymbol${String.format(Locale.US, "%.2f", stats.statementBalance)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "${stats.statementWindowStart.formatToHumanReadable()} - ${stats.statementWindowEnd.formatToHumanReadable()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Current Balance", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$currencySymbol${String.format(Locale.US, "%.2f", stats.currentBalance)}", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Remaining", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$currencySymbol${String.format(Locale.US, "%.2f", stats.remainingLimit)}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            LinearProgressIndicator(
                progress = { (stats.utilizationPercentage / 100).coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(6.dp),
                color = if (stats.utilizationPercentage > 80) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
